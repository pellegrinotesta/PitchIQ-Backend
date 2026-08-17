ALTER TABLE giocatori
ADD COLUMN nazionalita       VARCHAR(50)       NULL,
ADD COLUMN luogo_nascita     VARCHAR(100)      NULL,
ADD COLUMN altezza_cm        SMALLINT UNSIGNED NULL,
ADD COLUMN peso_kg           SMALLINT UNSIGNED NULL,
ADD COLUMN ruoli_secondari   VARCHAR(100)      NULL,
ADD COLUMN stipendio_annuo   DECIMAL(12,2)     NULL,
ADD COLUMN valore_mercato    DECIMAL(12,2)     NULL,
ADD COLUMN clausola          DECIMAL(12,2)     NULL,
ADD COLUMN note_mediche      VARCHAR(500)      NULL,
ADD COLUMN agente            VARCHAR(100)      NULL;