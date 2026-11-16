-- Ruolo per lo staff sanitario: unico, insieme ad ADMIN, a leggere le note mediche.
ALTER TABLE utenti
    MODIFY COLUMN ruolo ENUM('ALLENATORE','DIRIGENTE','MEDICO','ADMIN') NOT NULL DEFAULT 'ALLENATORE';
