"""Scrittura su MySQL. Le tabelle le crea Flyway nel BFF: qui solo DML."""
from __future__ import annotations

import json
import os
from datetime import datetime, timezone

from sqlalchemy import create_engine, text
from sqlalchemy.engine import Engine

from .pipeline import MatchResult


def engine_from_env() -> Engine:
    url = os.getenv("ANALYTICS_DB_URL")  # usato nei test (sqlite)
    if not url:
        url = ("mysql+pymysql://{user}:{pwd}@{host}:{port}/{db}?charset=utf8mb4".format(
            user=os.environ["DB_USERNAME"], pwd=os.environ["DB_PASSWORD"],
            host=os.getenv("DB_HOST", "host.docker.internal"), port=os.getenv("DB_PORT", "3307"),
            db=os.getenv("DB_NAME", "pitch_iq")))
    return create_engine(url, pool_pre_ping=True, future=True)


def _now() -> datetime:
    return datetime.now(timezone.utc).replace(tzinfo=None)


def _dump(v) -> str:
    return json.dumps(v, separators=(",", ":"), ensure_ascii=False)


def set_status(engine: Engine, import_id: int, stato: str, messaggio: str | None = None,
               match_id: int | None = None) -> None:
    with engine.begin() as cn:
        cn.execute(text("""UPDATE tracking_import
                           SET stato = :stato, messaggio = :msg,
                               match_id = COALESCE(:mid, match_id)
                           WHERE id = :id"""),  # aggiornato_il: ON UPDATE CURRENT_TIMESTAMP
                   dict(stato=stato, msg=(messaggio or None) and messaggio[:500], mid=match_id,
                        id=import_id))


def save_result(engine: Engine, import_id: int, r: MatchResult) -> None:
    """Sostituisce per intero i dati della partita, in un'unica transazione."""
    p = r.partita
    with engine.begin() as cn:
        mid = p["match_id"]
        # ricaricare la stessa partita la sostituisce
        cn.execute(text("""DELETE FROM tracking_frame WHERE sequenza_id IN
                           (SELECT id FROM tracking_sequenza WHERE match_id = :m)"""), dict(m=mid))
        cn.execute(text("DELETE FROM tracking_sequenza WHERE match_id = :m"), dict(m=mid))
        cn.execute(text("DELETE FROM tracking_partita WHERE match_id = :m"), dict(m=mid))

        cn.execute(text("""INSERT INTO tracking_partita
            (match_id, import_id, data_partita, squadra_casa, squadra_trasferta, gol_casa,
             gol_trasferta, competizione, giocatori_json, elaborata_il)
            VALUES (:match_id, :import_id, :data_partita, :squadra_casa, :squadra_trasferta,
                    :gol_casa, :gol_trasferta, :competizione, :giocatori_json, :now)"""),
            dict({k: v for k, v in p.items() if k != "giocatori"}, import_id=import_id,
                 giocatori_json=_dump(p["giocatori"]), now=_now()))

        if r.sequenze:
            cn.execute(text("""INSERT INTO tracking_sequenza
                (match_id, possesso_id, esito, periodo, minuto, secondo, team_id, squadra,
                 portatore_id, portatore, terzo, n_pressori, n_opzioni, valore_portatore,
                 valore_medio, valore_migliore, esito_possesso, porta_al_tiro, ha_frame)
                VALUES (:match_id, :possesso_id, :esito, :periodo, :minuto, :secondo, :team_id,
                        :squadra, :portatore_id, :portatore, :terzo, :n_pressori, :n_opzioni,
                        :valore_portatore, :valore_medio, :valore_migliore, :esito_possesso,
                        :porta_al_tiro, :ha_frame)"""),
                [dict(s, match_id=mid, ha_frame=bool(s["ha_frame"])) for s in r.sequenze])

        if r.animazioni:
            ids = dict(cn.execute(text(
                "SELECT possesso_id, id FROM tracking_sequenza WHERE match_id = :m"), dict(m=mid)).all())
            cn.execute(text("""INSERT INTO tracking_frame
                (sequenza_id, pressori_json, press_inizio, press_fine, frames_json)
                VALUES (:sid, :pressori, :pi, :pf, :frames)"""),
                [dict(sid=ids[pid], pressori=_dump(a["pressori"]), pi=a["press_inizio"],
                      pf=a["press_fine"], frames=_dump(a["frames"]))
                 for pid, a in r.animazioni.items()])
