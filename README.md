# PitchIQ Backend

Backend della piattaforma PitchIQ, composto da un BFF REST in Spring Boot e da un servizio interno di analytics per l'elaborazione dei dati di tracking.

## Componenti

- `PitchIQ/`: API REST, autenticazione JWT, accesso a MySQL e migrazioni Flyway.
- `pitchiq-analytics/`: servizio FastAPI che elabora le importazioni di tracking richieste dal BFF.

Il BFF ascolta sulla porta `8080`; il servizio analytics sulla porta `8000`. Il frontend comunica con il BFF, mentre analytics viene chiamato internamente dal BFF.

## Prerequisiti

- Java 24 per l'esecuzione locale del BFF.
- MySQL raggiungibile su `localhost:3307`, con database `pitch_iq`.
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

Il file Compose si trova nella radice del repository, non in questa cartella. Avvia MySQL sull'host alla porta `3307`, crea il database `pitch_iq` e crea un file `.env` nella radice del repository con:

```dotenv
DB_USERNAME=il_tuo_utente
DB_PASSWORD=la_tua_password
JWT_SECRET=imposta_una_chiave_sicura_di_almeno_32_caratteri
CORS_ORIGINS=http://localhost:4200
```

Dalla radice del repository:

```bash
docker compose up --build
```

Per avviare anche il microfrontend tracking:

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

Il BFF sarà disponibile su `http://localhost:8080`; analytics su `http://localhost:8000/health`. Per arrestare i container, esegui `docker compose down` dalla radice.

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
- Test end-to-end analytics su SQLite: dalla cartella `pitchiq-analytics`, esegui `python -m tests.e2e_sqlite <cartella_con_i_file_della_partita>`.