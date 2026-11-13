ALTER TABLE statistiche_partita
    MODIFY COLUMN possesso_pct DECIMAL(4,1) NULL,
    ADD COLUMN fonte VARCHAR(255) NULL;
