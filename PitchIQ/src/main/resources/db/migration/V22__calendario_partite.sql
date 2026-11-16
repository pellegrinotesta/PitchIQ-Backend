-- Calendario: partite ancora da giocare, orario d'inizio e report di scouting dell'avversario.
ALTER TABLE partite
    ADD COLUMN giocata     BOOLEAN NOT NULL DEFAULT TRUE,
    ADD COLUMN ora_inizio  TIME NULL,
    ADD COLUMN scouting_id BIGINT NULL,
    ADD CONSTRAINT fk_partita_scouting FOREIGN KEY (scouting_id)
        REFERENCES scouting_avversari (id) ON DELETE SET NULL;
