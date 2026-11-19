# PitchIQ Backend

Backend della piattaforma PitchIQ, composto da un BFF REST in Spring Boot e da un servizio interno di analytics per l'elaborazione dei dati di tracking.

## Componenti

- `PitchIQ/`: API REST, autenticazione JWT, accesso a MySQL e migrazioni Flyway.
- `pitchiq-analytics/`: servizio FastAPI che elabora le importazioni di tracking richieste dal BFF.
- L'analisi video sperimentale estrae clip candidate in base al movimento visivo; non classifica ancora le singole azioni.

Il BFF ascolta sulla porta `8080`; il servizio analytics sulla porta `8000`. Il frontend comunica con il BFF, mentre analytics viene chiamato internamente dal BFF.

## Prerequisiti

- Java 24 per l'esecuzione locale del BFF.
- MySQL raggiungibile su `localhost:3307` per l'esecuzione locale del BFF e di analytics.
- Python 3.12 per l'esecuzione locale del servizio analytics.
- Docker e Docker Compose per avviare i servizi containerizzati.

## Configurazione

Per l'avvio locale del BFF, configura queste variabili d'ambiente:

| Variabile | Descrizione |
| --- | --- |
| `DB_USERNAME` | Utente MySQL |
| `DB_PASSWORD` | Password MySQL |
| `JWT_SECRET` | Chiave JWT; usa un valore sicuro di almeno 32 caratteri |
| `CORS_ORIGINS` | Origini frontend consentite, per esempio `http://localhost:4200` |
| `ANALYTICS_URL` | URL del servizio analytics; default `http://localhost:8000` |

Flyway applica automaticamente le migrazioni all'avvio. Il datasource locale usa `localhost:3307/pitch_iq`.

## Avvio con Docker Compose

Il file Compose si trova nella radice del repository, non in questa cartella. Compose avvia MySQL 8.4 in un container e crea il database `pitch_iq` su un nuovo volume persistente. Crea un file `.env` nella radice del repository con:

```dotenv
DB_USERNAME=pitchiq
DB_PASSWORD=imposta_una_password_mysql_sicura
JWT_SECRET=imposta_una_chiave_sicura_di_almeno_32_caratteri
CORS_ORIGINS=http://localhost:4200
```

Dalla radice del repository avvia tutti i moduli, incluso tracking:

```bash
docker compose --profile tracking up --build
```

Se il file `.env` si trova invece in `PitchIQ-Backend`, caricalo esplicitamente dalla radice:

```bash
docker compose --env-file ./PitchIQ-Backend/.env --profile tracking up --build
```

`JWT_SECRET` deve essere non vuoto e contenere almeno 32 byte UTF-8 casuali.
Compose interrompe subito l'avvio se la variabile manca o e' vuota.
Non pubblicare il file `.env` e mantieni stabile la chiave tra i riavvii.

La shell sarà disponibile su `http://localhost:4200`; il microfrontend tracking su
`http://localhost:4206`; BFF su `http://localhost:8080` e analytics su
`http://localhost:8000/health`. Non usare `docker compose down -v` se vuoi conservare il database.

## Avvio locale

Avvia prima MySQL e imposta le variabili richieste dal BFF. Da `PitchIQ-Backend/PitchIQ`:

```bash
./mvnw spring-boot:run
```

Su Windows usa `mvnw.cmd spring-boot:run`.

Per avviare analytics, da `PitchIQ-Backend/pitchiq-analytics`:

```bash
python -m venv .venv
python -m pip install -r requirements.txt
python -m uvicorn app.main:app --reload --host 0.0.0.0 --port 8000
```

In locale, analytics usa per impostazione predefinita `host.docker.internal:3307` per MySQL. Se necessario, configura `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USERNAME` e `DB_PASSWORD` per il tuo ambiente.

## API e verifiche

- Swagger UI: `http://localhost:8080/swagger-ui/index.html`
- OpenAPI: `http://localhost:8080/v3/api-docs`
- Health analytics: `http://localhost:8000/health`
- Test BFF: dalla cartella `PitchIQ`, esegui `./mvnw test` (su Windows `mvnw.cmd test`).
- Test del rilevamento video: dalla cartella `pitchiq-analytics`, esegui `python -m unittest tests.test_video_pipeline`.
- Test end-to-end analytics su SQLite: dalla cartella `pitchiq-analytics`, esegui `python -m tests.e2e_sqlite <cartella_con_i_file_della_partita>`.

L'upload video accetta MP4, MOV e WebM fino a 250 MB e 2 minuti. Le clip generate mostrano un indice
relativo di movimento, non una confidenza di riconoscimento; per classificare passaggi, tiri,
dribbling, contrasti, intercetti e gol serve integrare o addestrare un modello dedicato.

### Dataset video per il riconoscimento delle azioni

Per preparare un dataset iniziale dal SoccerNet Ball Action Spotting 2024:

1. Richiedi l'accesso e firma l'NDA sul [sito ufficiale SoccerNet](https://www.soccer-net.org/data).
   Il repository di codice è MIT, ma questo **non** concede una licenza sui video; non scaricare,
   distribuire o usare i filmati fuori dai termini accettati.
2. Installa il downloader ufficiale: `python -m pip install SoccerNet`.
3. Dalla cartella `PitchIQ-Backend/pitchiq-analytics`, esegui:

   ```bash
   python tools/prepare_soccernet_dataset.py `
     --source data/soccernet `
     --output data/prepared-soccernet `
     --download
   ```

   Su shell POSIX sostituisci i backtick di PowerShell con `\` a fine riga. Lo script chiede la
   password NDA senza salvarla, scarica i soli split train/valid/test e genera `annotations.jsonl`
   e `summary.json`. I video restano nella cartella sorgente e non vengono copiati.

Il dataset pubblico disponibile per le ball actions copre sette partite riprese in modalità
broadcast, non riprese tattiche fisse. Contiene passaggi, tiri, gol e contrasti riusciti; `Drive`
viene mantenuto come classe distinta (non equivale automaticamente a dribbling) e non contiene una
classe d'intercetto. Il riepilogo segnala esplicitamente le classi senza annotazioni utilizzabili.
Per completare le sei classi richieste e addestrare sul tipo di ripresa desiderato occorrono
annotazioni aggiuntive, soprattutto intercetti e riprese tattiche. Il dataset seed da solo non
consente di certificare un classificatore completo.

La normalizzazione si può verificare senza scaricare il dataset:
`python -B -m unittest tests.test_prepare_soccernet_dataset tests.test_video_pipeline`.