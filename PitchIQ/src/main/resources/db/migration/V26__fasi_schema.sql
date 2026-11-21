-- Fasi di gioco dello schema (modello tattico riusabile): posizioni per nome dello slot e frecce.
-- Applicando lo schema a una formazione, gli slot diventano i giocatori assegnati.
ALTER TABLE schemi_formazione
    ADD COLUMN fasi JSON NULL;
