-- Formazione preparata per una partita del calendario (facoltativo: restano possibili formazioni generiche).
ALTER TABLE formazioni
    ADD COLUMN partita_id BIGINT NULL,
    ADD CONSTRAINT fk_formazione_partita FOREIGN KEY (partita_id) REFERENCES partite (id) ON DELETE SET NULL;
