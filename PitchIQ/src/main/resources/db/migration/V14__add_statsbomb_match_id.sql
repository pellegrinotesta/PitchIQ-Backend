ALTER TABLE partite
    ADD COLUMN statsbomb_match_id INT NULL,
    ADD UNIQUE INDEX uk_partite_statsbomb_match_id (statsbomb_match_id);