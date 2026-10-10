"""
Calcolo delle sequenze di pressione da file SkillCorner (match.json, dynamic_events.csv,
tracking .jsonl opzionale). Funzioni pure: non toccano il database.

Domanda: superare (o no) la pressione sul portatore cambia il valore delle opzioni
di passaggio disponibili?
  - possesso pressato: almeno un on_ball_engagement di pressione collegato al possesso
  - esito: SUPERATA / NEUTRALIZZATA / CONTENUTA
  - valore di un'opzione: xthreat * xpass_completion (minaccia attesa)
"""
from __future__ import annotations

import json
from dataclasses import dataclass, field
from pathlib import Path

import numpy as np
import pandas as pd

PRESS_SUBTYPES = {"pressure", "pressing", "counter_press", "recovery_press"}
PRE_FRAMES = 30      # 3 s prima dell'inizio della pressione (10 fps)
POST_FRAMES = 20     # 2 s dopo la fine del possesso
SAMPLE_PER_ESITO = 15

REQUIRED_EVENT_COLUMNS = {
    "event_id", "event_type", "event_subtype", "frame_start", "frame_end", "period",
    "minute_start", "second_start", "team_id", "team_shortname", "player_id", "player_name",
    "associated_player_possession_event_id", "beaten_by_possession", "beaten_by_movement",
    "stop_possession_danger", "force_backward", "end_type", "xthreat", "xpass_completion",
    "possession_epv_for_start", "lead_to_shot", "third_start", "attacking_side", "match_id",
}


class InvalidInput(ValueError):
    """Errore nei file caricati: il messaggio finisce nell'app così com'è."""


@dataclass
class MatchResult:
    partita: dict
    sequenze: list[dict]
    # chiave: possesso_id -> dati animazione
    animazioni: dict[str, dict] = field(default_factory=dict)


# ---------------------------------------------------------------- input

def find_files(job_dir: Path) -> tuple[Path, Path, Path | None]:
    def one(pattern: str, label: str, required: bool = True):
        found = sorted(job_dir.glob(pattern))
        if not found and required:
            raise InvalidInput(f"Manca il file {label}.")
        return found[0] if found else None

    return (one("*match*.json", "della partita (match.json)"),
            one("*dynamic_events*.csv", "degli eventi (dynamic_events.csv)"),
            one("*.jsonl", "di tracking", required=False))


def load_inputs(match_path: Path, events_path: Path) -> tuple[dict, pd.DataFrame]:
    try:
        meta = json.loads(match_path.read_text())
    except json.JSONDecodeError as e:
        raise InvalidInput(f"Il file della partita non è un JSON valido ({e.msg}).") from e
    for key in ("id", "home_team", "away_team", "players", "date_time"):
        if key not in meta:
            raise InvalidInput(f"Il file della partita non sembra di SkillCorner: manca '{key}'.")

    try:
        ev = pd.read_csv(events_path, low_memory=False)
    except Exception as e:  # noqa: BLE001 - qualunque errore di parsing va mostrato all'utente
        raise InvalidInput(f"Il file degli eventi non è un CSV leggibile ({e}).") from e
    missing = REQUIRED_EVENT_COLUMNS - set(ev.columns)
    if missing:
        raise InvalidInput("Il file degli eventi non è un dynamic_events SkillCorner: "
                           f"mancano le colonne {', '.join(sorted(missing)[:5])}.")
    ids = set(ev.match_id.dropna().astype(int))
    if ids and ids != {int(meta["id"])}:
        raise InvalidInput(f"Gli eventi sono della partita {', '.join(map(str, ids))}, "
                           f"il file della partita è la {meta['id']}.")
    return meta, ev


def stream_tracking(path: Path, wanted: set[int]) -> dict[int, dict]:
    """Legge il JSONL riga per riga e tiene solo i frame che servono (~90 MB a partita)."""
    out: dict[int, dict] = {}
    with path.open() as fh:
        for n, line in enumerate(fh):
            if n == 0 and line.startswith("version https://git-lfs"):
                raise InvalidInput("Il file di tracking è un puntatore Git LFS, non i dati. "
                                   "Scaricalo con 'git lfs pull'.")
            head = line[:40]
            try:
                f = int(head[head.index(":") + 1:head.index(",")])
            except ValueError:
                continue
            if f in wanted:
                out[f] = json.loads(line)
    if not out:
        raise InvalidInput("Il file di tracking non contiene i frame delle sequenze: "
                           "è della stessa partita?")
    return out


# ---------------------------------------------------------------- calcolo

def _bool(s: pd.Series) -> pd.Series:
    return s.astype(str).eq("True")


def build_sequences(meta: dict, ev: pd.DataFrame) -> tuple[pd.DataFrame, pd.DataFrame]:
    eng = ev[(ev.event_type == "on_ball_engagement")
             & ev.event_subtype.isin(PRESS_SUBTYPES)
             & ev.associated_player_possession_event_id.notna()].copy()
    if eng.empty:
        raise InvalidInput("Nel file degli eventi non ci sono pressioni sul portatore.")
    eng["beaten"] = _bool(eng.beaten_by_possession) | _bool(eng.beaten_by_movement)
    eng["neutral"] = (eng.end_type.notna() | _bool(eng.stop_possession_danger)
                      | _bool(eng.force_backward))

    g = eng.groupby("associated_player_possession_event_id").agg(
        beaten=("beaten", "any"), neutral=("neutral", "any"),
        n_pressori=("player_id", "nunique"),
        press_start=("frame_start", "min"), press_end=("frame_end", "max"),
    )
    pressori = eng.groupby("associated_player_possession_event_id").player_id.unique()

    pos = ev[ev.event_type == "player_possession"].set_index("event_id")
    opt = ev[ev.event_type == "passing_option"].copy()
    opt["valore"] = opt.xthreat.fillna(0) * opt.xpass_completion.fillna(0)
    opt_stats = opt.groupby("associated_player_possession_event_id").valore.agg(["size", "mean", "max"])

    rows = []
    for pid, r in g.iterrows():
        if pid not in pos.index:
            continue
        p = pos.loc[pid]
        n_opt, v_mean, v_max = (opt_stats.loc[pid] if pid in opt_stats.index else (0, 0.0, 0.0))
        rows.append(dict(
            possesso_id=pid,
            esito="SUPERATA" if r.beaten else ("NEUTRALIZZATA" if r.neutral else "CONTENUTA"),
            periodo=int(p.period),
            minuto=int(p.minute_start),
            secondo=int(p.second_start),
            team_id=int(p.team_id),
            squadra=str(p.team_shortname),
            portatore_id=int(p.player_id),
            portatore=str(p.player_name),
            terzo=None if pd.isna(p.third_start) else str(p.third_start),
            n_pressori=int(r.n_pressori),
            n_opzioni=int(n_opt),
            valore_portatore=None if pd.isna(p.possession_epv_for_start) else round(float(p.possession_epv_for_start), 4),
            valore_medio=round(float(v_mean), 4),
            valore_migliore=round(float(v_max), 4),
            esito_possesso=None if pd.isna(p.end_type) else str(p.end_type),
            porta_al_tiro=str(p.lead_to_shot) == "True",
            # campi di lavoro, non vanno a DB
            w_attacking_side=p.attacking_side,
            w_frame_inizio=int(min(r.press_start, p.frame_start) - PRE_FRAMES),
            w_frame_fine=int(p.frame_end + POST_FRAMES),
            w_press_start=int(r.press_start),
            w_press_end=int(r.press_end),
            w_pressori=[int(x) for x in pressori.loc[pid]],
        ))
    return pd.DataFrame(rows), opt


def pick_highlights(seq: pd.DataFrame) -> pd.DataFrame:
    """Animazione per tutte le SUPERATE + le altre con le opzioni di maggior valore."""
    sup = seq[seq.esito == "SUPERATA"]
    altre = (seq[seq.esito != "SUPERATA"].sort_values("valore_migliore", ascending=False)
             .groupby("esito").head(SAMPLE_PER_ESITO))
    return pd.concat([sup, altre])


def build_animation(meta: dict, s: pd.Series, opt_pos: pd.DataFrame, tracking: dict) -> dict | None:
    team_of = {pl["id"]: pl["team_id"] for pl in meta["players"]}
    sign = -1 if s["w_attacking_side"] == "right_to_left" else 1  # l'attacco va sempre verso destra
    o_start = opt_pos.frame_start.to_numpy()
    o_end = opt_pos.frame_end.to_numpy()
    o_pid = opt_pos.player_id.astype(int).astype(str).to_numpy()
    o_val = opt_pos.valore.round(4).to_numpy()

    frames = []
    for f in range(s["w_frame_inizio"], s["w_frame_fine"] + 1):
        t = tracking.get(f)
        if not t or not t.get("player_data"):
            continue
        active = np.flatnonzero((o_start <= f) & (o_end >= f))
        ball = t["ball_data"]
        frames.append(dict(
            i=f - s["w_frame_inizio"],
            b=None if ball.get("x") is None else [round(sign * ball["x"], 2), round(sign * ball["y"], 2)],
            c=t["possession"]["player_id"],
            p=[[pl["player_id"], 1 if team_of.get(pl["player_id"]) == s.team_id else 0,
                round(sign * pl["x"], 2), round(sign * pl["y"], 2)] for pl in t["player_data"]],
            o={o_pid[k]: float(o_val[k]) for k in active},
        ))
    if len(frames) < 10:
        return None
    return dict(pressori=s["w_pressori"],
                press_inizio=s["w_press_start"] - s["w_frame_inizio"],
                press_fine=s["w_press_end"] - s["w_frame_inizio"],
                frames=frames)


def process(job_dir: Path) -> MatchResult:
    match_path, events_path, tracking_path = find_files(job_dir)
    meta, ev = load_inputs(match_path, events_path)
    seq, opt = build_sequences(meta, ev)

    animazioni: dict[str, dict] = {}
    if tracking_path is not None:
        hl = pick_highlights(seq)
        wanted = {f for r in hl.itertuples() for f in range(r.w_frame_inizio, r.w_frame_fine + 1)}
        trk = stream_tracking(tracking_path, wanted)
        opt_by_pos = dict(tuple(opt.groupby("associated_player_possession_event_id")))
        empty = opt.iloc[0:0]
        for _, s in hl.iterrows():
            anim = build_animation(meta, s, opt_by_pos.get(s.possesso_id, empty), trk)
            if anim:
                animazioni[s.possesso_id] = anim

    seq["ha_frame"] = seq.possesso_id.isin(animazioni.keys())
    public = [c for c in seq.columns if not c.startswith("w_")]
    return MatchResult(
        partita=dict(
            match_id=int(meta["id"]),
            data_partita=meta["date_time"][:19].replace("T", " "),
            squadra_casa=meta["home_team"]["short_name"],
            squadra_trasferta=meta["away_team"]["short_name"],
            gol_casa=int(meta.get("home_team_score") or 0),
            gol_trasferta=int(meta.get("away_team_score") or 0),
            competizione=(meta.get("competition_edition") or {}).get("name"),
            giocatori={str(pl["id"]): dict(nome=pl.get("short_name") or pl.get("last_name"),
                                           teamId=pl["team_id"], numero=pl.get("number"))
                       for pl in meta["players"]},
        ),
        sequenze=seq[public].to_dict("records"),
        animazioni=animazioni,
    )
