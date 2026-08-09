CREATE TABLE utenti
(
    id            BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
    username      VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    ruolo         ENUM('ALLENATORE','DIRIGENTE','ADMIN') NOT NULL DEFAULT 'ALLENATORE'
);

-- Password: "admin123" hashata con BCrypt strength 12. Cambiare in produzione.
INSERT INTO utenti (username, password_hash, ruolo)
VALUES ('admin', '$2a$12$7QJ8n3Fz1mKpLgWxR2hOBuDvE9sNcXtYaI5oP0kMjHqSeVbZwA1UC', 'ADMIN');