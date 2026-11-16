-- Carico di lavoro per seduta: minuti svolti e sforzo percepito (RPE, scala 1-10).
ALTER TABLE presenze
    ADD COLUMN minuti SMALLINT UNSIGNED NULL,
    ADD COLUMN rpe    TINYINT NULL CHECK (rpe BETWEEN 1 AND 10);
