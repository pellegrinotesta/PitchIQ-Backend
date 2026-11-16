-- Dati per la verifica della lista dei 25: formazione nel vivaio e giocatori esclusi dalla lista.
ALTER TABLE giocatori
    ADD COLUMN formazione  ENUM('CLUB','ITALIA','ALTRO') NULL,
    ADD COLUMN fuori_lista BOOLEAN NOT NULL DEFAULT FALSE;
