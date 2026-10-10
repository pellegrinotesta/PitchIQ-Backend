"""Test end-to-end su SQLite (da PitchIQ-Backend/pitchiq-analytics):
    python -m tests.e2e_sqlite <cartella partita SkillCorner>
"""
import os, re, shutil, sys, tempfile, time, json
from pathlib import Path

src = Path(sys.argv[1]); work = Path(tempfile.mkdtemp())
os.environ["TRACKING_UPLOAD_DIR"] = str(work / "uploads")
os.environ["ANALYTICS_DB_URL"] = f"sqlite:///{work}/t.db"
from sqlalchemy import create_engine, text
# PitchIQ-Backend/pitchiq-analytics/tests -> PitchIQ-Backend/PitchIQ/src/main/resources/db/migration
mig_dir = Path(__file__).resolve().parents[2] / "PitchIQ/src/main/resources/db/migration"
found = sorted(mig_dir.glob("V*__tracking_schema.sql"))
if not found:
    sys.exit(f"Migration V*__tracking_schema.sql non trovata in {mig_dir}")
sql = found[0]
ddl = re.sub(r"--[^\n]*", "", sql.read_text())
ddl = ddl.replace("BIGINT        NOT NULL AUTO_INCREMENT PRIMARY KEY", "INTEGER PRIMARY KEY AUTOINCREMENT")
ddl = ddl.replace(" ON UPDATE CURRENT_TIMESTAMP(6)", "").replace("CURRENT_TIMESTAMP(6)", "CURRENT_TIMESTAMP"); ddl = re.sub(r",\s*INDEX [^\n]*", "", ddl); ddl = re.sub(r"\n\s*INDEX [^\n]*", "", ddl)
eng = create_engine(os.environ["ANALYTICS_DB_URL"])
with eng.begin() as cn:
    for stmt in [s for s in ddl.split(";") if s.strip()]:
        cn.exec_driver_sql(stmt)

from fastapi.testclient import TestClient
from app import main

def new_import(cn):
    cn.execute(text("INSERT INTO tracking_import (stato, con_tracking, nome_file_eventi, creato_il, aggiornato_il) VALUES ('IN_CODA',1,'x',CURRENT_TIMESTAMP,CURRENT_TIMESTAMP)"))
    return cn.execute(text("SELECT max(id) FROM tracking_import")).scalar()

c = TestClient(main.app)
for round_ in (1, 2):   # la seconda volta verifica la sostituzione della stessa partita
    with eng.begin() as cn: iid = new_import(cn)
    d = work / "uploads" / str(iid); d.mkdir(parents=True)
    for f in src.iterdir():
        if f.suffix in (".json", ".csv", ".jsonl") and "phases" not in f.name: shutil.copy(f, d)
    t = time.time(); r = c.post("/jobs", json={"importId": iid, "cartella": str(iid)}); print(r.status_code, r.json())
    main.executor.shutdown(wait=True); main.executor = __import__("concurrent.futures").futures.ThreadPoolExecutor(1)
    with eng.connect() as cn:
        print(f"round {round_}: {time.time()-t:.1f}s", cn.execute(text("SELECT stato, messaggio, match_id FROM tracking_import WHERE id=:i"), dict(i=iid)).one())
        print(" sequenze", cn.execute(text("SELECT esito, count(*) FROM tracking_sequenza GROUP BY esito")).all())
        print(" frame", cn.execute(text("SELECT count(*), sum(length(frames_json)) FROM tracking_frame")).one(), "cartella rimossa:", not d.exists())

# errori: cartella inesistente, eventi sbagliati
print(c.post("/jobs", json={"importId": 99, "cartella": "../etc"}).status_code)
with eng.begin() as cn: iid = new_import(cn)
d = work / "uploads" / str(iid); d.mkdir()
(d / "x_match.json").write_text(json.dumps({"id": 1}))
(d / "x_dynamic_events.csv").write_text("a,b\n1,2\n")
main.run_job(iid, d)
with eng.connect() as cn: print(cn.execute(text("SELECT stato, messaggio FROM tracking_import WHERE id=:i"), dict(i=iid)).one())
