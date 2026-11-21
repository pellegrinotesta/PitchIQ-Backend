-- Fasi di gioco della formazione (possesso, non possesso, transizioni): posizioni dei titolari e frecce
-- dei movimenti per ogni fase. Si leggono e salvano sempre insieme alla formazione: un JSON basta.
ALTER TABLE formazioni
    ADD COLUMN fasi JSON NULL;
