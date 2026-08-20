ALTER TABLE giocatori
MODIFY COLUMN ruolo ENUM(
    'POR',
    'DC','TSD','TSS','LB',
    'CDC','CC','MOC','ALD','ALS','W',
    'PC','SP','FW'
) NOT NULL;