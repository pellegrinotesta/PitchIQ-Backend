ALTER TABLE partita_convocati
    ADD COLUMN posizione_partita VARCHAR(20) NULL,
    ADD COLUMN numero_maglia_partita SMALLINT UNSIGNED NULL,
    ADD COLUMN modulo_fonte VARCHAR(20) NULL;

ALTER TABLE eventi_partita
    ADD COLUMN periodo TINYINT UNSIGNED NULL,
    ADD COLUMN minuto_aggiuntivo TINYINT UNSIGNED NULL,
    ADD COLUMN riferimento_esterno VARCHAR(64) NULL,
    ADD UNIQUE KEY uk_evento_riferimento_esterno (riferimento_esterno);
