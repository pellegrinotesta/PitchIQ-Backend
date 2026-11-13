-- Fonte: ESPN. Le posizioni rappresentano ruoli, non coordinate di tracking.
-- L'importazione conserva eventi manuali e posizioni gia presenti.
START TRANSACTION;

UPDATE partita_convocati pc
JOIN partite p ON p.id=pc.partita_id
JOIN giocatori g ON g.id=pc.giocatore_id
JOIN (
    SELECT '2026-08-14' data_partita,'Monza' avversario,'Coppa Italia 2026/27' competizione,'TRASFERTA' casa,0 fatti,3 subiti,50 maglia,'Martinelli' cognome,'G' posizione,'4-3-1-2' modulo
    UNION ALL SELECT '2026-08-14','Monza','Coppa Italia 2026/27','TRASFERTA',0,3,44,'Simic','CD-L','4-3-1-2'
    UNION ALL SELECT '2026-08-14','Monza','Coppa Italia 2026/27','TRASFERTA',0,3,4,'Izzo','CD-R','4-3-1-2'
    UNION ALL SELECT '2026-08-14','Monza','Coppa Italia 2026/27','TRASFERTA',0,3,27,'Moruzzi','LB','4-3-1-2'
    UNION ALL SELECT '2026-08-14','Monza','Coppa Italia 2026/27','TRASFERTA',0,3,29,'Cancellotti','RB','4-3-1-2'
    UNION ALL SELECT '2026-08-14','Monza','Coppa Italia 2026/27','TRASFERTA',0,3,14,'Biasci','M','4-3-1-2'
    UNION ALL SELECT '2026-08-14','Monza','Coppa Italia 2026/27','TRASFERTA',0,3,6,'Palmiero','CM','4-3-1-2'
    UNION ALL SELECT '2026-08-14','Monza','Coppa Italia 2026/27','TRASFERTA',0,3,10,'Russo','AM','4-3-1-2'
    UNION ALL SELECT '2026-08-14','Monza','Coppa Italia 2026/27','TRASFERTA',0,3,39,'Besaggio','LM','4-3-1-2'
    UNION ALL SELECT '2026-08-14','Monza','Coppa Italia 2026/27','TRASFERTA',0,3,20,'Palumbo','RM','4-3-1-2'
    UNION ALL SELECT '2026-08-14','Monza','Coppa Italia 2026/27','TRASFERTA',0,3,11,'Fila','CF-R','4-3-1-2'
    UNION ALL SELECT '2026-08-22','Arezzo','Serie B 2026/27','CASA',1,2,50,'Martinelli','G','4-2-3-1'
    UNION ALL SELECT '2026-08-22','Arezzo','Serie B 2026/27','CASA',1,2,44,'Simic','CD-L','4-2-3-1'
    UNION ALL SELECT '2026-08-22','Arezzo','Serie B 2026/27','CASA',1,2,4,'Izzo','CD-R','4-2-3-1'
    UNION ALL SELECT '2026-08-22','Arezzo','Serie B 2026/27','CASA',1,2,27,'Moruzzi','LB','4-2-3-1'
    UNION ALL SELECT '2026-08-22','Arezzo','Serie B 2026/27','CASA',1,2,29,'Cancellotti','RB','4-2-3-1'
    UNION ALL SELECT '2026-08-22','Arezzo','Serie B 2026/27','CASA',1,2,14,'Biasci','AM','4-2-3-1'
    UNION ALL SELECT '2026-08-22','Arezzo','Serie B 2026/27','CASA',1,2,39,'Besaggio','LM','4-2-3-1'
    UNION ALL SELECT '2026-08-22','Arezzo','Serie B 2026/27','CASA',1,2,6,'Palmiero','RM','4-2-3-1'
    UNION ALL SELECT '2026-08-22','Arezzo','Serie B 2026/27','CASA',1,2,11,'Fila','F','4-2-3-1'
    UNION ALL SELECT '2026-08-22','Arezzo','Serie B 2026/27','CASA',1,2,10,'Russo','AM-L','4-2-3-1'
    UNION ALL SELECT '2026-08-22','Arezzo','Serie B 2026/27','CASA',1,2,20,'Palumbo','AM-R','4-2-3-1'
    UNION ALL SELECT '2026-08-29','Vicenza','Serie B 2026/27','CASA',3,0,50,'Martinelli','G','3-5-2'
    UNION ALL SELECT '2026-08-29','Vicenza','Serie B 2026/27','CASA',3,0,44,'Simic','CD','3-5-2'
    UNION ALL SELECT '2026-08-29','Vicenza','Serie B 2026/27','CASA',3,0,56,'Enrici','CD-L','3-5-2'
    UNION ALL SELECT '2026-08-29','Vicenza','Serie B 2026/27','CASA',3,0,4,'Izzo','CD-R','3-5-2'
    UNION ALL SELECT '2026-08-29','Vicenza','Serie B 2026/27','CASA',3,0,39,'Besaggio','CM-L','3-5-2'
    UNION ALL SELECT '2026-08-29','Vicenza','Serie B 2026/27','CASA',3,0,20,'Palumbo','CM-R','3-5-2'
    UNION ALL SELECT '2026-08-29','Vicenza','Serie B 2026/27','CASA',3,0,6,'Palmiero','AM','3-5-2'
    UNION ALL SELECT '2026-08-29','Vicenza','Serie B 2026/27','CASA',3,0,27,'Moruzzi','LM','3-5-2'
    UNION ALL SELECT '2026-08-29','Vicenza','Serie B 2026/27','CASA',3,0,29,'Cancellotti','RM','3-5-2'
    UNION ALL SELECT '2026-08-29','Vicenza','Serie B 2026/27','CASA',3,0,99,'Favilli','CF-L','3-5-2'
    UNION ALL SELECT '2026-08-29','Vicenza','Serie B 2026/27','CASA',3,0,10,'Russo','CF-R','3-5-2'
    UNION ALL SELECT '2026-09-06','Modena','Serie B 2026/27','TRASFERTA',1,0,50,'Martinelli','G','3-5-2'
    UNION ALL SELECT '2026-09-06','Modena','Serie B 2026/27','TRASFERTA',1,0,44,'Simic','CD','3-5-2'
    UNION ALL SELECT '2026-09-06','Modena','Serie B 2026/27','TRASFERTA',1,0,56,'Enrici','CD-L','3-5-2'
    UNION ALL SELECT '2026-09-06','Modena','Serie B 2026/27','TRASFERTA',1,0,4,'Izzo','CD-R','3-5-2'
    UNION ALL SELECT '2026-09-06','Modena','Serie B 2026/27','TRASFERTA',1,0,39,'Besaggio','CM-L','3-5-2'
    UNION ALL SELECT '2026-09-06','Modena','Serie B 2026/27','TRASFERTA',1,0,8,'Di Maggio','CM-R','3-5-2'
    UNION ALL SELECT '2026-09-06','Modena','Serie B 2026/27','TRASFERTA',1,0,20,'Palumbo','AM','3-5-2'
    UNION ALL SELECT '2026-09-06','Modena','Serie B 2026/27','TRASFERTA',1,0,27,'Moruzzi','LM','3-5-2'
    UNION ALL SELECT '2026-09-06','Modena','Serie B 2026/27','TRASFERTA',1,0,2,'Missori','RM','3-5-2'
    UNION ALL SELECT '2026-09-06','Modena','Serie B 2026/27','TRASFERTA',1,0,99,'Favilli','CF-L','3-5-2'
    UNION ALL SELECT '2026-09-06','Modena','Serie B 2026/27','TRASFERTA',1,0,10,'Russo','CF-R','3-5-2'
    UNION ALL SELECT '2026-09-13','Palermo','Serie B 2026/27','CASA',1,1,50,'Martinelli','G','3-5-2'
    UNION ALL SELECT '2026-09-13','Palermo','Serie B 2026/27','CASA',1,1,23,'Venturi','CD','3-5-2'
    UNION ALL SELECT '2026-09-13','Palermo','Serie B 2026/27','CASA',1,1,56,'Enrici','CD-L','3-5-2'
    UNION ALL SELECT '2026-09-13','Palermo','Serie B 2026/27','CASA',1,1,4,'Izzo','CD-R','3-5-2'
    UNION ALL SELECT '2026-09-13','Palermo','Serie B 2026/27','CASA',1,1,20,'Palumbo','CM-L','3-5-2'
    UNION ALL SELECT '2026-09-13','Palermo','Serie B 2026/27','CASA',1,1,8,'Di Maggio','CM-R','3-5-2'
    UNION ALL SELECT '2026-09-13','Palermo','Serie B 2026/27','CASA',1,1,39,'Besaggio','AM','3-5-2'
    UNION ALL SELECT '2026-09-13','Palermo','Serie B 2026/27','CASA',1,1,27,'Moruzzi','LM','3-5-2'
    UNION ALL SELECT '2026-09-13','Palermo','Serie B 2026/27','CASA',1,1,29,'Cancellotti','RM','3-5-2'
    UNION ALL SELECT '2026-09-13','Palermo','Serie B 2026/27','CASA',1,1,10,'Russo','CF-L','3-5-2'
    UNION ALL SELECT '2026-09-13','Palermo','Serie B 2026/27','CASA',1,1,7,'Pandolfi','CF-R','3-5-2'
    UNION ALL SELECT '2026-09-19','Ascoli','Serie B 2026/27','TRASFERTA',1,3,50,'Martinelli','G','3-5-2'
    UNION ALL SELECT '2026-09-19','Ascoli','Serie B 2026/27','TRASFERTA',1,3,23,'Venturi','CD','3-5-2'
    UNION ALL SELECT '2026-09-19','Ascoli','Serie B 2026/27','TRASFERTA',1,3,56,'Enrici','CD-L','3-5-2'
    UNION ALL SELECT '2026-09-19','Ascoli','Serie B 2026/27','TRASFERTA',1,3,4,'Izzo','CD-R','3-5-2'
    UNION ALL SELECT '2026-09-19','Ascoli','Serie B 2026/27','TRASFERTA',1,3,39,'Besaggio','CM-L','3-5-2'
    UNION ALL SELECT '2026-09-19','Ascoli','Serie B 2026/27','TRASFERTA',1,3,8,'Di Maggio','CM-R','3-5-2'
    UNION ALL SELECT '2026-09-19','Ascoli','Serie B 2026/27','TRASFERTA',1,3,20,'Palumbo','AM','3-5-2'
    UNION ALL SELECT '2026-09-19','Ascoli','Serie B 2026/27','TRASFERTA',1,3,27,'Moruzzi','LM','3-5-2'
    UNION ALL SELECT '2026-09-19','Ascoli','Serie B 2026/27','TRASFERTA',1,3,2,'Missori','RM','3-5-2'
    UNION ALL SELECT '2026-09-19','Ascoli','Serie B 2026/27','TRASFERTA',1,3,10,'Russo','CF-L','3-5-2'
    UNION ALL SELECT '2026-09-19','Ascoli','Serie B 2026/27','TRASFERTA',1,3,9,'Cheddira','CF-R','3-5-2'
    UNION ALL SELECT '2026-10-09','Sampdoria','Serie B 2026/27','CASA',4,1,50,'Martinelli','G','3-5-2'
    UNION ALL SELECT '2026-10-09','Sampdoria','Serie B 2026/27','CASA',4,1,23,'Venturi','CD','3-5-2'
    UNION ALL SELECT '2026-10-09','Sampdoria','Serie B 2026/27','CASA',4,1,56,'Enrici','CD-L','3-5-2'
    UNION ALL SELECT '2026-10-09','Sampdoria','Serie B 2026/27','CASA',4,1,4,'Izzo','CD-R','3-5-2'
    UNION ALL SELECT '2026-10-09','Sampdoria','Serie B 2026/27','CASA',4,1,39,'Besaggio','CM-L','3-5-2'
    UNION ALL SELECT '2026-10-09','Sampdoria','Serie B 2026/27','CASA',4,1,20,'Palumbo','CM-R','3-5-2'
    UNION ALL SELECT '2026-10-09','Sampdoria','Serie B 2026/27','CASA',4,1,6,'Palmiero','AM','3-5-2'
    UNION ALL SELECT '2026-10-09','Sampdoria','Serie B 2026/27','CASA',4,1,27,'Moruzzi','LM','3-5-2'
    UNION ALL SELECT '2026-10-09','Sampdoria','Serie B 2026/27','CASA',4,1,2,'Missori','RM','3-5-2'
    UNION ALL SELECT '2026-10-09','Sampdoria','Serie B 2026/27','CASA',4,1,9,'Cheddira','CF-L','3-5-2'
    UNION ALL SELECT '2026-10-09','Sampdoria','Serie B 2026/27','CASA',4,1,10,'Russo','CF-R','3-5-2'
    UNION ALL SELECT '2026-10-18','Cesena','Serie B 2026/27','TRASFERTA',1,1,50,'Martinelli','G','3-5-2'
    UNION ALL SELECT '2026-10-18','Cesena','Serie B 2026/27','TRASFERTA',1,1,23,'Venturi','CD','3-5-2'
    UNION ALL SELECT '2026-10-18','Cesena','Serie B 2026/27','TRASFERTA',1,1,56,'Enrici','CD-L','3-5-2'
    UNION ALL SELECT '2026-10-18','Cesena','Serie B 2026/27','TRASFERTA',1,1,4,'Izzo','CD-R','3-5-2'
    UNION ALL SELECT '2026-10-18','Cesena','Serie B 2026/27','TRASFERTA',1,1,39,'Besaggio','CM-L','3-5-2'
    UNION ALL SELECT '2026-10-18','Cesena','Serie B 2026/27','TRASFERTA',1,1,20,'Palumbo','CM-R','3-5-2'
    UNION ALL SELECT '2026-10-18','Cesena','Serie B 2026/27','TRASFERTA',1,1,6,'Palmiero','AM','3-5-2'
    UNION ALL SELECT '2026-10-18','Cesena','Serie B 2026/27','TRASFERTA',1,1,27,'Moruzzi','LM','3-5-2'
    UNION ALL SELECT '2026-10-18','Cesena','Serie B 2026/27','TRASFERTA',1,1,2,'Missori','RM','3-5-2'
    UNION ALL SELECT '2026-10-18','Cesena','Serie B 2026/27','TRASFERTA',1,1,9,'Cheddira','CF-L','3-5-2'
    UNION ALL SELECT '2026-10-18','Cesena','Serie B 2026/27','TRASFERTA',1,1,10,'Russo','CF-R','3-5-2'
    UNION ALL SELECT '2026-10-24','Padova','Serie B 2026/27','CASA',0,1,50,'Martinelli','G','3-5-2'
    UNION ALL SELECT '2026-10-24','Padova','Serie B 2026/27','CASA',0,1,23,'Venturi','CD','3-5-2'
    UNION ALL SELECT '2026-10-24','Padova','Serie B 2026/27','CASA',0,1,56,'Enrici','CD-L','3-5-2'
    UNION ALL SELECT '2026-10-24','Padova','Serie B 2026/27','CASA',0,1,5,'Di Bitonto','CD-R','3-5-2'
    UNION ALL SELECT '2026-10-24','Padova','Serie B 2026/27','CASA',0,1,39,'Besaggio','CM-L','3-5-2'
    UNION ALL SELECT '2026-10-24','Padova','Serie B 2026/27','CASA',0,1,20,'Palumbo','CM-R','3-5-2'
    UNION ALL SELECT '2026-10-24','Padova','Serie B 2026/27','CASA',0,1,6,'Palmiero','AM','3-5-2'
    UNION ALL SELECT '2026-10-24','Padova','Serie B 2026/27','CASA',0,1,27,'Moruzzi','LM','3-5-2'
    UNION ALL SELECT '2026-10-24','Padova','Serie B 2026/27','CASA',0,1,2,'Missori','RM','3-5-2'
    UNION ALL SELECT '2026-10-24','Padova','Serie B 2026/27','CASA',0,1,10,'Russo','CF-L','3-5-2'
    UNION ALL SELECT '2026-10-24','Padova','Serie B 2026/27','CASA',0,1,9,'Cheddira','CF-R','3-5-2'
    UNION ALL SELECT '2026-10-27','Virtus Entella','Serie B 2026/27','TRASFERTA',1,1,50,'Martinelli','G','3-5-2'
    UNION ALL SELECT '2026-10-27','Virtus Entella','Serie B 2026/27','TRASFERTA',1,1,23,'Venturi','CD','3-5-2'
    UNION ALL SELECT '2026-10-27','Virtus Entella','Serie B 2026/27','TRASFERTA',1,1,56,'Enrici','CD-L','3-5-2'
    UNION ALL SELECT '2026-10-27','Virtus Entella','Serie B 2026/27','TRASFERTA',1,1,4,'Izzo','CD-R','3-5-2'
    UNION ALL SELECT '2026-10-27','Virtus Entella','Serie B 2026/27','TRASFERTA',1,1,24,'Sounas','CM-L','3-5-2'
    UNION ALL SELECT '2026-10-27','Virtus Entella','Serie B 2026/27','TRASFERTA',1,1,8,'Di Maggio','CM-R','3-5-2'
    UNION ALL SELECT '2026-10-27','Virtus Entella','Serie B 2026/27','TRASFERTA',1,1,16,'Faticanti','AM','3-5-2'
    UNION ALL SELECT '2026-10-27','Virtus Entella','Serie B 2026/27','TRASFERTA',1,1,27,'Moruzzi','LM','3-5-2'
    UNION ALL SELECT '2026-10-27','Virtus Entella','Serie B 2026/27','TRASFERTA',1,1,2,'Missori','RM','3-5-2'
    UNION ALL SELECT '2026-10-27','Virtus Entella','Serie B 2026/27','TRASFERTA',1,1,14,'Biasci','CF-L','3-5-2'
    UNION ALL SELECT '2026-10-27','Virtus Entella','Serie B 2026/27','TRASFERTA',1,1,99,'Favilli','CF-R','3-5-2'
    UNION ALL SELECT '2026-11-01','Pisa','Serie B 2026/27','CASA',2,0,50,'Martinelli','G','3-5-2'
    UNION ALL SELECT '2026-11-01','Pisa','Serie B 2026/27','CASA',2,0,23,'Venturi','CD','3-5-2'
    UNION ALL SELECT '2026-11-01','Pisa','Serie B 2026/27','CASA',2,0,56,'Enrici','CD-L','3-5-2'
    UNION ALL SELECT '2026-11-01','Pisa','Serie B 2026/27','CASA',2,0,4,'Izzo','CD-R','3-5-2'
    UNION ALL SELECT '2026-11-01','Pisa','Serie B 2026/27','CASA',2,0,39,'Besaggio','CM-L','3-5-2'
    UNION ALL SELECT '2026-11-01','Pisa','Serie B 2026/27','CASA',2,0,8,'Di Maggio','CM-R','3-5-2'
    UNION ALL SELECT '2026-11-01','Pisa','Serie B 2026/27','CASA',2,0,20,'Palumbo','AM','3-5-2'
    UNION ALL SELECT '2026-11-01','Pisa','Serie B 2026/27','CASA',2,0,27,'Moruzzi','LM','3-5-2'
    UNION ALL SELECT '2026-11-01','Pisa','Serie B 2026/27','CASA',2,0,2,'Missori','RM','3-5-2'
    UNION ALL SELECT '2026-11-01','Pisa','Serie B 2026/27','CASA',2,0,10,'Russo','CF-L','3-5-2'
    UNION ALL SELECT '2026-11-01','Pisa','Serie B 2026/27','CASA',2,0,9,'Cheddira','CF-R','3-5-2'
    UNION ALL SELECT '2026-11-07','Benevento','Serie B 2026/27','TRASFERTA',1,1,50,'Martinelli','G','3-5-2'
    UNION ALL SELECT '2026-11-07','Benevento','Serie B 2026/27','TRASFERTA',1,1,23,'Venturi','CD','3-5-2'
    UNION ALL SELECT '2026-11-07','Benevento','Serie B 2026/27','TRASFERTA',1,1,56,'Enrici','CD-L','3-5-2'
    UNION ALL SELECT '2026-11-07','Benevento','Serie B 2026/27','TRASFERTA',1,1,4,'Izzo','CD-R','3-5-2'
    UNION ALL SELECT '2026-11-07','Benevento','Serie B 2026/27','TRASFERTA',1,1,39,'Besaggio','CM-L','3-5-2'
    UNION ALL SELECT '2026-11-07','Benevento','Serie B 2026/27','TRASFERTA',1,1,8,'Di Maggio','CM-R','3-5-2'
    UNION ALL SELECT '2026-11-07','Benevento','Serie B 2026/27','TRASFERTA',1,1,20,'Palumbo','AM','3-5-2'
    UNION ALL SELECT '2026-11-07','Benevento','Serie B 2026/27','TRASFERTA',1,1,27,'Moruzzi','LM','3-5-2'
    UNION ALL SELECT '2026-11-07','Benevento','Serie B 2026/27','TRASFERTA',1,1,2,'Missori','RM','3-5-2'
    UNION ALL SELECT '2026-11-07','Benevento','Serie B 2026/27','TRASFERTA',1,1,10,'Russo','CF-L','3-5-2'
    UNION ALL SELECT '2026-11-07','Benevento','Serie B 2026/27','TRASFERTA',1,1,9,'Cheddira','CF-R','3-5-2'
) s ON p.data=s.data_partita AND p.avversario=s.avversario AND p.competizione=s.competizione
    AND p.casa_trasferta=s.casa AND p.gol_fatti=s.fatti AND p.gol_subiti=s.subiti
    AND g.numero_maglia=s.maglia AND g.cognome=s.cognome
SET pc.posizione_partita=s.posizione, pc.numero_maglia_partita=s.maglia, pc.modulo_fonte=s.modulo
WHERE pc.titolare=TRUE AND pc.posizione_partita IS NULL AND pc.numero_maglia_partita IS NULL;

INSERT INTO eventi_partita (partita_id,minuto,minuto_aggiuntivo,periodo,tipo,riferimento_esterno,nota,giocatore_id)
SELECT p.id,s.minuto,s.aggiuntivo,s.periodo,s.tipo,s.riferimento,s.nota,
       (SELECT CASE WHEN COUNT(*)=1 THEN MIN(g.id) ELSE NULL END FROM giocatori g
        WHERE CONCAT(g.nome,' ',g.cognome)=s.giocatore)
FROM (
    SELECT '2026-08-14' data_partita,'Monza' avversario,'Coppa Italia 2026/27' competizione,'TRASFERTA' casa,0 fatti,3 subiti,22 minuto,0 aggiuntivo,1 periodo,'GOL' tipo,'espn:50848309' riferimento,'Gustavo Varela - Monza' nota,NULL giocatore
    UNION ALL SELECT '2026-08-14','Monza','Coppa Italia 2026/27','TRASFERTA',0,3,45,0,1,'GOL','espn:50849766','Gustavo Varela - Monza',NULL
    UNION ALL SELECT '2026-08-14','Monza','Coppa Italia 2026/27','TRASFERTA',0,3,47,0,2,'AMMONIZIONE','espn:50851085','Brando Moruzzi - Avellino','Brando Moruzzi'
    UNION ALL SELECT '2026-08-14','Monza','Coppa Italia 2026/27','TRASFERTA',0,3,51,0,2,'GOL','espn:50851316','Omari Forson - Monza',NULL
    UNION ALL SELECT '2026-08-14','Monza','Coppa Italia 2026/27','TRASFERTA',0,3,55,0,2,'SOSTITUZIONE_IN','espn:50851446','Luca Pandolfi entra, Daniel Fila esce - Avellino','Luca Pandolfi'
    UNION ALL SELECT '2026-08-14','Monza','Coppa Italia 2026/27','TRASFERTA',0,3,55,0,2,'SOSTITUZIONE_IN','espn:50851471','Andrea Favilli entra, Tommaso Biasci esce - Avellino','Andrea Favilli'
    UNION ALL SELECT '2026-08-14','Monza','Coppa Italia 2026/27','TRASFERTA',0,3,59,0,2,'SOSTITUZIONE_IN','espn:50851784','Luca Di Maggio entra, Martin Palumbo esce - Avellino','Luca Di Maggio'
    UNION ALL SELECT '2026-08-14','Monza','Coppa Italia 2026/27','TRASFERTA',0,3,59,0,2,'SOSTITUZIONE_IN','espn:50851785','Dimitris Sounas entra, Luca Palmiero esce - Avellino','Dimitris Sounas'
    UNION ALL SELECT '2026-08-14','Monza','Coppa Italia 2026/27','TRASFERTA',0,3,70,0,2,'SOSTITUZIONE_IN','espn:50852290','Andrea Colpani entra, Omari Forson esce - Monza',NULL
    UNION ALL SELECT '2026-08-14','Monza','Coppa Italia 2026/27','TRASFERTA',0,3,70,0,2,'SOSTITUZIONE_IN','espn:50852335','Adam Bakoune entra, Ricardo Mangas esce - Monza',NULL
    UNION ALL SELECT '2026-08-14','Monza','Coppa Italia 2026/27','TRASFERTA',0,3,73,0,2,'SOSTITUZIONE_IN','espn:50852371','Michael Venturi entra, Lorenco Simic esce - Avellino','Michael Venturi'
    UNION ALL SELECT '2026-08-14','Monza','Coppa Italia 2026/27','TRASFERTA',0,3,81,0,2,'SOSTITUZIONE_IN','espn:50852753','Andrea Petagna entra, Gustavo Varela esce - Monza',NULL
    UNION ALL SELECT '2026-08-14','Monza','Coppa Italia 2026/27','TRASFERTA',0,3,81,0,2,'SOSTITUZIONE_IN','espn:50852754','Lorenzo Lucchesi entra, Samuele Birindelli esce - Monza',NULL
    UNION ALL SELECT '2026-08-14','Monza','Coppa Italia 2026/27','TRASFERTA',0,3,85,0,2,'SOSTITUZIONE_IN','espn:50852970','Nicolas Galazzi entra, Mathis Mout esce - Monza',NULL
    UNION ALL SELECT '2026-08-14','Monza','Coppa Italia 2026/27','TRASFERTA',0,3,90,1,2,'AMMONIZIONE','espn:50853433','Dimitris Sounas - Avellino','Dimitris Sounas'
    UNION ALL SELECT '2026-08-22','Arezzo','Serie B 2026/27','CASA',1,2,8,0,1,'AMMONIZIONE','espn:51261537','Gennaro Iaccarino - Arezzo',NULL
    UNION ALL SELECT '2026-08-22','Arezzo','Serie B 2026/27','CASA',1,2,12,0,1,'SOSTITUZIONE_IN','espn:51262030','Andrea Favilli entra, Daniel Fila esce - Avellino','Andrea Favilli'
    UNION ALL SELECT '2026-08-22','Arezzo','Serie B 2026/27','CASA',1,2,15,0,1,'GOL','espn:51262641','Raffaele Russo - Avellino','Raffaele Russo'
    UNION ALL SELECT '2026-08-22','Arezzo','Serie B 2026/27','CASA',1,2,54,0,2,'AMMONIZIONE','espn:51270871','Michele Besaggio - Avellino','Michele Besaggio'
    UNION ALL SELECT '2026-08-22','Arezzo','Serie B 2026/27','CASA',1,2,56,0,2,'GOL','espn:51271158','Pietro Cianci - Arezzo (rigore)',NULL
    UNION ALL SELECT '2026-08-22','Arezzo','Serie B 2026/27','CASA',1,2,63,0,2,'AMMONIZIONE','espn:51272238','Mattia Sala - Arezzo',NULL
    UNION ALL SELECT '2026-08-22','Arezzo','Serie B 2026/27','CASA',1,2,63,0,2,'SOSTITUZIONE_IN','espn:51272441','Luca Di Maggio entra, Martin Palumbo esce - Avellino','Luca Di Maggio'
    UNION ALL SELECT '2026-08-22','Arezzo','Serie B 2026/27','CASA',1,2,64,0,2,'SOSTITUZIONE_IN','espn:51272442','Dimitris Sounas entra, Michele Besaggio esce - Avellino','Dimitris Sounas'
    UNION ALL SELECT '2026-08-22','Arezzo','Serie B 2026/27','CASA',1,2,64,0,2,'SOSTITUZIONE_IN','espn:51272523','Marco Sala entra, Brando Moruzzi esce - Avellino','Marco Sala'
    UNION ALL SELECT '2026-08-22','Arezzo','Serie B 2026/27','CASA',1,2,66,0,2,'SOSTITUZIONE_IN','espn:51272866','Shaka Mawuli entra, Mattia Sala esce - Arezzo',NULL
    UNION ALL SELECT '2026-08-22','Arezzo','Serie B 2026/27','CASA',1,2,77,0,2,'SOSTITUZIONE_IN','espn:51274634','Emiliano Pattarello entra, Alessandro Arena esce - Arezzo',NULL
    UNION ALL SELECT '2026-08-22','Arezzo','Serie B 2026/27','CASA',1,2,77,0,2,'SOSTITUZIONE_IN','espn:51274635','Muhamed Djamanca entra, Pietro Cianci esce - Arezzo',NULL
    UNION ALL SELECT '2026-08-22','Arezzo','Serie B 2026/27','CASA',1,2,78,0,2,'AMMONIZIONE','espn:51274693','Luca Palmiero - Avellino','Luca Palmiero'
    UNION ALL SELECT '2026-08-22','Arezzo','Serie B 2026/27','CASA',1,2,82,0,2,'SOSTITUZIONE_IN','espn:51275055','Mattia Viviani entra, Gennaro Iaccarino esce - Arezzo',NULL
    UNION ALL SELECT '2026-08-22','Arezzo','Serie B 2026/27','CASA',1,2,83,0,2,'SOSTITUZIONE_IN','espn:51275204','Luca Pandolfi entra, Tommaso Biasci esce - Avellino','Luca Pandolfi'
    UNION ALL SELECT '2026-08-22','Arezzo','Serie B 2026/27','CASA',1,2,90,0,2,'GOL','espn:51275996','Artur Ionita - Arezzo',NULL
    UNION ALL SELECT '2026-08-29','Vicenza','Serie B 2026/27','CASA',3,0,11,0,1,'GOL','espn:51640836','Michele Besaggio - Avellino','Michele Besaggio'
    UNION ALL SELECT '2026-08-29','Vicenza','Serie B 2026/27','CASA',3,0,16,0,1,'GOL','espn:51641343','Raffaele Russo - Avellino','Raffaele Russo'
    UNION ALL SELECT '2026-08-29','Vicenza','Serie B 2026/27','CASA',3,0,30,0,1,'AMMONIZIONE','espn:51642959','Gabriele Moncini - Vicenza',NULL
    UNION ALL SELECT '2026-08-29','Vicenza','Serie B 2026/27','CASA',3,0,38,0,1,'AMMONIZIONE','espn:51643462','Michele Besaggio - Avellino','Michele Besaggio'
    UNION ALL SELECT '2026-08-29','Vicenza','Serie B 2026/27','CASA',3,0,45,0,1,'AMMONIZIONE','espn:51643756','Armando Izzo - Avellino','Armando Izzo'
    UNION ALL SELECT '2026-08-29','Vicenza','Serie B 2026/27','CASA',3,0,45,0,2,'SOSTITUZIONE_IN','espn:51645339','Raul Talarico entra, Marco Carraro esce - Vicenza',NULL
    UNION ALL SELECT '2026-08-29','Vicenza','Serie B 2026/27','CASA',3,0,45,0,2,'SOSTITUZIONE_IN','espn:51645340','Lorenzo Caferri entra, Alessandro Pietrelli esce - Vicenza',NULL
    UNION ALL SELECT '2026-08-29','Vicenza','Serie B 2026/27','CASA',3,0,45,0,2,'SOSTITUZIONE_IN','espn:51645425','Claudio Morra entra, Gabriele Moncini esce - Vicenza',NULL
    UNION ALL SELECT '2026-08-29','Vicenza','Serie B 2026/27','CASA',3,0,46,0,2,'GOL','espn:51645461','Brando Moruzzi - Avellino','Brando Moruzzi'
    UNION ALL SELECT '2026-08-29','Vicenza','Serie B 2026/27','CASA',3,0,57,0,2,'SOSTITUZIONE_IN','espn:51646634','Nicola Rauti entra, Maxime Leverbe esce - Vicenza',NULL
    UNION ALL SELECT '2026-08-29','Vicenza','Serie B 2026/27','CASA',3,0,64,0,2,'SOSTITUZIONE_IN','espn:51647477','Dimitris Sounas entra, Michele Besaggio esce - Avellino','Dimitris Sounas'
    UNION ALL SELECT '2026-08-29','Vicenza','Serie B 2026/27','CASA',3,0,64,0,2,'SOSTITUZIONE_IN','espn:51647497','Walid Cheddira entra, Andrea Favilli esce - Avellino','Walid Cheddira'
    UNION ALL SELECT '2026-08-29','Vicenza','Serie B 2026/27','CASA',3,0,64,0,2,'SOSTITUZIONE_IN','espn:51647498','Luca Di Maggio entra, Luca Palmiero esce - Avellino','Luca Di Maggio'
    UNION ALL SELECT '2026-08-29','Vicenza','Serie B 2026/27','CASA',3,0,66,0,2,'AMMONIZIONE','espn:51647699','Lorenco Simic - Avellino','Lorenco Simic'
    UNION ALL SELECT '2026-08-29','Vicenza','Serie B 2026/27','CASA',3,0,70,0,2,'SOSTITUZIONE_IN','espn:51648270','Tommaso Corazza entra, Filippo Costa esce - Vicenza',NULL
    UNION ALL SELECT '2026-08-29','Vicenza','Serie B 2026/27','CASA',3,0,77,0,2,'SOSTITUZIONE_IN','espn:51648821','Tommaso Biasci entra, Raffaele Russo esce - Avellino','Tommaso Biasci'
    UNION ALL SELECT '2026-08-29','Vicenza','Serie B 2026/27','CASA',3,0,79,0,2,'AMMONIZIONE','espn:51649032','Nicola Rauti - Vicenza',NULL
    UNION ALL SELECT '2026-08-29','Vicenza','Serie B 2026/27','CASA',3,0,83,0,2,'AMMONIZIONE','espn:51649473','Loris Zonta - Vicenza',NULL
    UNION ALL SELECT '2026-08-29','Vicenza','Serie B 2026/27','CASA',3,0,83,0,2,'SOSTITUZIONE_IN','espn:51649520','Giacomo Faticanti entra, Martin Palumbo esce - Avellino','Giacomo Faticanti'
    UNION ALL SELECT '2026-08-29','Vicenza','Serie B 2026/27','CASA',3,0,86,0,2,'AMMONIZIONE','espn:51649768','Giacomo Faticanti - Avellino','Giacomo Faticanti'
    UNION ALL SELECT '2026-09-06','Modena','Serie B 2026/27','TRASFERTA',1,0,8,0,1,'GOL','espn:52002295','Michele Besaggio - Avellino','Michele Besaggio'
    UNION ALL SELECT '2026-09-06','Modena','Serie B 2026/27','TRASFERTA',1,0,19,0,1,'SOSTITUZIONE_IN','espn:52003823','Walid Cheddira entra, Andrea Favilli esce - Avellino','Walid Cheddira'
    UNION ALL SELECT '2026-09-06','Modena','Serie B 2026/27','TRASFERTA',1,0,34,0,1,'AMMONIZIONE','espn:52006132','Francesco Zampano - Modena',NULL
    UNION ALL SELECT '2026-09-06','Modena','Serie B 2026/27','TRASFERTA',1,0,38,0,1,'AMMONIZIONE','espn:52006702','Lorenco Simic - Avellino','Lorenco Simic'
    UNION ALL SELECT '2026-09-06','Modena','Serie B 2026/27','TRASFERTA',1,0,45,0,2,'SOSTITUZIONE_IN','espn:52009796','Paulo Azzi entra, Francesco Zampano esce - Modena',NULL
    UNION ALL SELECT '2026-09-06','Modena','Serie B 2026/27','TRASFERTA',1,0,45,0,2,'SOSTITUZIONE_IN','espn:52009795','Tommaso Cancellotti entra, Filippo Missori esce - Avellino','Tommaso Cancellotti'
    UNION ALL SELECT '2026-09-06','Modena','Serie B 2026/27','TRASFERTA',1,0,45,0,2,'SOSTITUZIONE_IN','espn:52010159','Michael Venturi entra, Lorenco Simic esce - Avellino','Michael Venturi'
    UNION ALL SELECT '2026-09-06','Modena','Serie B 2026/27','TRASFERTA',1,0,63,0,2,'SOSTITUZIONE_IN','espn:52012736','Luca Bacchin entra, Gastón Brugman esce - Modena',NULL
    UNION ALL SELECT '2026-09-06','Modena','Serie B 2026/27','TRASFERTA',1,0,65,0,2,'SOSTITUZIONE_IN','espn:52012976','Dimitris Sounas entra, Michele Besaggio esce - Avellino','Dimitris Sounas'
    UNION ALL SELECT '2026-09-06','Modena','Serie B 2026/27','TRASFERTA',1,0,76,0,2,'AMMONIZIONE','espn:52014676','Giuseppe Ambrosino - Modena',NULL
    UNION ALL SELECT '2026-09-06','Modena','Serie B 2026/27','TRASFERTA',1,0,77,0,2,'SOSTITUZIONE_IN','espn:52014880','Thomas Berenbruch entra, Luca Di Maggio esce - Avellino','Thomas Berenbruch'
    UNION ALL SELECT '2026-09-06','Modena','Serie B 2026/27','TRASFERTA',1,0,77,0,2,'SOSTITUZIONE_IN','espn:52014881','Daniele Montevago entra, Guiseppe Caso esce - Modena',NULL
    UNION ALL SELECT '2026-09-06','Modena','Serie B 2026/27','TRASFERTA',1,0,86,0,2,'SOSTITUZIONE_IN','espn:52016542','Kleis Bozhanaj entra, Simone Santoro esce - Modena',NULL
    UNION ALL SELECT '2026-09-06','Modena','Serie B 2026/27','TRASFERTA',1,0,86,0,2,'SOSTITUZIONE_IN','espn:52016410','Francesco Folino entra, Folly Nador esce - Modena',NULL
    UNION ALL SELECT '2026-09-06','Modena','Serie B 2026/27','TRASFERTA',1,0,87,0,2,'AMMONIZIONE','espn:52016543','Kleis Bozhanaj - Modena',NULL
    UNION ALL SELECT '2026-09-06','Modena','Serie B 2026/27','TRASFERTA',1,0,90,0,2,'AMMONIZIONE','espn:52017002','Tomasso Martinelli - Avellino','Tomasso Martinelli'
    UNION ALL SELECT '2026-09-13','Palermo','Serie B 2026/27','CASA',1,1,16,0,1,'SOSTITUZIONE_IN','espn:52359452','Filippo Missori entra, Tommaso Cancellotti esce - Avellino','Filippo Missori'
    UNION ALL SELECT '2026-09-13','Palermo','Serie B 2026/27','CASA',1,1,35,0,1,'AMMONIZIONE','espn:52360908','Mattia Bani - Palermo',NULL
    UNION ALL SELECT '2026-09-13','Palermo','Serie B 2026/27','CASA',1,1,56,0,2,'AMMONIZIONE','espn:52364017','Filippo Missori - Avellino','Filippo Missori'
    UNION ALL SELECT '2026-09-13','Palermo','Serie B 2026/27','CASA',1,1,57,0,2,'SOSTITUZIONE_IN','espn:52364086','Walid Cheddira entra, Luca Pandolfi esce - Avellino','Walid Cheddira'
    UNION ALL SELECT '2026-09-13','Palermo','Serie B 2026/27','CASA',1,1,59,0,2,'SOSTITUZIONE_IN','espn:52364243','Dennis Johnsen entra, Emmanuel Gyasi esce - Palermo',NULL
    UNION ALL SELECT '2026-09-13','Palermo','Serie B 2026/27','CASA',1,1,59,0,2,'SOSTITUZIONE_IN','espn:52364244','Nahuel Estévez entra, Jacopo Segre esce - Palermo',NULL
    UNION ALL SELECT '2026-09-13','Palermo','Serie B 2026/27','CASA',1,1,60,0,2,'GOL','espn:52364246','Walid Cheddira - Avellino','Walid Cheddira'
    UNION ALL SELECT '2026-09-13','Palermo','Serie B 2026/27','CASA',1,1,63,0,2,'GOL','espn:52364620','Dennis Johnsen - Palermo',NULL
    UNION ALL SELECT '2026-09-13','Palermo','Serie B 2026/27','CASA',1,1,76,0,2,'SOSTITUZIONE_IN','espn:52365456','Claudio Gomes entra, Filippo Ranocchia esce - Palermo',NULL
    UNION ALL SELECT '2026-09-13','Palermo','Serie B 2026/27','CASA',1,1,77,0,2,'SOSTITUZIONE_IN','espn:52365457','Alessandro Gabrielloni entra, Joel Pohjanpalo esce - Palermo',NULL
    UNION ALL SELECT '2026-09-13','Palermo','Serie B 2026/27','CASA',1,1,84,0,2,'AMMONIZIONE','espn:52365947','Michael Venturi - Avellino','Michael Venturi'
    UNION ALL SELECT '2026-09-13','Palermo','Serie B 2026/27','CASA',1,1,86,0,2,'SOSTITUZIONE_IN','espn:52366240','Marco Sala entra, Brando Moruzzi esce - Avellino','Marco Sala'
    UNION ALL SELECT '2026-09-13','Palermo','Serie B 2026/27','CASA',1,1,86,0,2,'SOSTITUZIONE_IN','espn:52366239','Dimitris Sounas entra, Luca Di Maggio esce - Avellino','Dimitris Sounas'
    UNION ALL SELECT '2026-09-13','Palermo','Serie B 2026/27','CASA',1,1,87,0,2,'SOSTITUZIONE_IN','espn:52366264','Tommaso Biasci entra, Raffaele Russo esce - Avellino','Tommaso Biasci'
    UNION ALL SELECT '2026-09-13','Palermo','Serie B 2026/27','CASA',1,1,87,0,2,'SOSTITUZIONE_IN','espn:52366365','Niccolò Pierozzi entra, Tommaso Cassandro esce - Palermo',NULL
    UNION ALL SELECT '2026-09-19','Ascoli','Serie B 2026/27','TRASFERTA',1,3,11,0,1,'AMMONIZIONE','espn:52630894','Luca Di Maggio - Avellino','Luca Di Maggio'
    UNION ALL SELECT '2026-09-19','Ascoli','Serie B 2026/27','TRASFERTA',1,3,17,0,1,'GOL','espn:52631649','Raffaele Russo - Avellino','Raffaele Russo'
    UNION ALL SELECT '2026-09-19','Ascoli','Serie B 2026/27','TRASFERTA',1,3,45,2,1,'AMMONIZIONE','espn:52635205','Nicholas Rizzo - Ascoli',NULL
    UNION ALL SELECT '2026-09-19','Ascoli','Serie B 2026/27','TRASFERTA',1,3,45,0,2,'SOSTITUZIONE_IN','espn:52636219','Marco Sala entra, Brando Moruzzi esce - Avellino','Marco Sala'
    UNION ALL SELECT '2026-09-19','Ascoli','Serie B 2026/27','TRASFERTA',1,3,49,0,2,'GOL','espn:52636536','Autogol di Patrick Enrici - gol Ascoli','Patrick Enrici'
    UNION ALL SELECT '2026-09-19','Ascoli','Serie B 2026/27','TRASFERTA',1,3,60,0,2,'AMMONIZIONE','espn:52637693','Giovanni Corradini - Ascoli',NULL
    UNION ALL SELECT '2026-09-19','Ascoli','Serie B 2026/27','TRASFERTA',1,3,66,0,2,'SOSTITUZIONE_IN','espn:52638325','Giacomo De Pieri entra, Don Bolsius esce - Ascoli',NULL
    UNION ALL SELECT '2026-09-19','Ascoli','Serie B 2026/27','TRASFERTA',1,3,66,0,2,'SOSTITUZIONE_IN','espn:52638365','Gabriele Gori entra, Giovanni Corradini esce - Ascoli',NULL
    UNION ALL SELECT '2026-09-19','Ascoli','Serie B 2026/27','TRASFERTA',1,3,67,0,2,'SOSTITUZIONE_IN','espn:52638366','Andrea Oliveri entra, Matteo Brunori Sandri esce - Ascoli',NULL
    UNION ALL SELECT '2026-09-19','Ascoli','Serie B 2026/27','TRASFERTA',1,3,68,0,2,'SOSTITUZIONE_IN','espn:52638565','Dimitris Sounas entra, Luca Di Maggio esce - Avellino','Dimitris Sounas'
    UNION ALL SELECT '2026-09-19','Ascoli','Serie B 2026/27','TRASFERTA',1,3,72,0,2,'GOL','espn:52639136','Andrea Silipo - Ascoli',NULL
    UNION ALL SELECT '2026-09-19','Ascoli','Serie B 2026/27','TRASFERTA',1,3,79,0,2,'AMMONIZIONE','espn:52640718','Michele Besaggio - Avellino','Michele Besaggio'
    UNION ALL SELECT '2026-09-19','Ascoli','Serie B 2026/27','TRASFERTA',1,3,80,0,2,'SOSTITUZIONE_IN','espn:52640771','Tommaso Biasci entra, Patrick Enrici esce - Avellino','Tommaso Biasci'
    UNION ALL SELECT '2026-09-19','Ascoli','Serie B 2026/27','TRASFERTA',1,3,80,0,2,'SOSTITUZIONE_IN','espn:52640772','Luca Pandolfi entra, Walid Cheddira esce - Avellino','Luca Pandolfi'
    UNION ALL SELECT '2026-09-19','Ascoli','Serie B 2026/27','TRASFERTA',1,3,81,0,2,'SOSTITUZIONE_IN','espn:52640955','Gennaro Acampora entra, Tommaso Milanese esce - Ascoli',NULL
    UNION ALL SELECT '2026-09-19','Ascoli','Serie B 2026/27','TRASFERTA',1,3,86,0,2,'GOL','espn:52641978','Andrea Silipo - Ascoli',NULL
    UNION ALL SELECT '2026-09-19','Ascoli','Serie B 2026/27','TRASFERTA',1,3,88,0,2,'SOSTITUZIONE_IN','espn:52642142','Federico Gattoni entra, Andrea Silipo esce - Ascoli',NULL
    UNION ALL SELECT '2026-09-19','Ascoli','Serie B 2026/27','TRASFERTA',1,3,88,0,2,'SOSTITUZIONE_IN','espn:52642197','Sebastiano Di Paolo entra, Michele Besaggio esce - Avellino','Sebastiano Di Paolo'
    UNION ALL SELECT '2026-10-09','Sampdoria','Serie B 2026/27','CASA',4,1,12,0,1,'GOL','espn:53335316','Walid Cheddira - Avellino','Walid Cheddira'
    UNION ALL SELECT '2026-10-09','Sampdoria','Serie B 2026/27','CASA',4,1,18,0,1,'GOL','espn:53335836','Lorenzo Insigne - Sampdoria',NULL
    UNION ALL SELECT '2026-10-09','Sampdoria','Serie B 2026/27','CASA',4,1,20,0,1,'GOL','espn:53335976','Martin Palumbo - Avellino','Martin Palumbo'
    UNION ALL SELECT '2026-10-09','Sampdoria','Serie B 2026/27','CASA',4,1,24,0,1,'GOL','espn:53336218','Raffaele Russo - Avellino','Raffaele Russo'
    UNION ALL SELECT '2026-10-09','Sampdoria','Serie B 2026/27','CASA',4,1,29,0,1,'GOL','espn:53336833','Michele Besaggio - Avellino','Michele Besaggio'
    UNION ALL SELECT '2026-10-09','Sampdoria','Serie B 2026/27','CASA',4,1,45,0,2,'SOSTITUZIONE_IN','espn:53340691','Alex Ferrari entra, Yann Karamoh esce - Sampdoria',NULL
    UNION ALL SELECT '2026-10-09','Sampdoria','Serie B 2026/27','CASA',4,1,59,0,2,'SOSTITUZIONE_IN','espn:53342369','Andrea Favilli entra, Walid Cheddira esce - Avellino','Andrea Favilli'
    UNION ALL SELECT '2026-10-09','Sampdoria','Serie B 2026/27','CASA',4,1,59,0,2,'SOSTITUZIONE_IN','espn:53342370','Giacomo Faticanti entra, Luca Palmiero esce - Avellino','Giacomo Faticanti'
    UNION ALL SELECT '2026-10-09','Sampdoria','Serie B 2026/27','CASA',4,1,61,0,2,'SOSTITUZIONE_IN','espn:53342452','Gennaro Tutino entra, Yari Verschaeren esce - Sampdoria',NULL
    UNION ALL SELECT '2026-10-09','Sampdoria','Serie B 2026/27','CASA',4,1,68,0,2,'SOSTITUZIONE_IN','espn:53343031','Tjas Begic entra, Tobias Lauritsen esce - Sampdoria',NULL
    UNION ALL SELECT '2026-10-09','Sampdoria','Serie B 2026/27','CASA',4,1,68,0,2,'SOSTITUZIONE_IN','espn:53343092','Fabio Depaoli entra, Liam Henderson esce - Sampdoria',NULL
    UNION ALL SELECT '2026-10-09','Sampdoria','Serie B 2026/27','CASA',4,1,71,0,2,'AMMONIZIONE','espn:53343256','Michael Venturi - Avellino','Michael Venturi'
    UNION ALL SELECT '2026-10-09','Sampdoria','Serie B 2026/27','CASA',4,1,76,0,2,'SOSTITUZIONE_IN','espn:53343437','Dimitris Sounas entra, Martin Palumbo esce - Avellino','Dimitris Sounas'
    UNION ALL SELECT '2026-10-09','Sampdoria','Serie B 2026/27','CASA',4,1,76,0,2,'SOSTITUZIONE_IN','espn:53343438','Tommaso Biasci entra, Raffaele Russo esce - Avellino','Tommaso Biasci'
    UNION ALL SELECT '2026-10-09','Sampdoria','Serie B 2026/27','CASA',4,1,77,0,2,'AMMONIZIONE','espn:53343517','Alex Ferrari - Sampdoria',NULL
    UNION ALL SELECT '2026-10-09','Sampdoria','Serie B 2026/27','CASA',4,1,81,0,2,'AMMONIZIONE','espn:53343940','Andrea Favilli - Avellino','Andrea Favilli'
    UNION ALL SELECT '2026-10-09','Sampdoria','Serie B 2026/27','CASA',4,1,85,0,2,'SOSTITUZIONE_IN','espn:53344310','Marco Sala entra, Brando Moruzzi esce - Avellino','Marco Sala'
    UNION ALL SELECT '2026-10-09','Sampdoria','Serie B 2026/27','CASA',4,1,90,1,2,'AMMONIZIONE','espn:53344836','Armando Izzo - Avellino','Armando Izzo'
    UNION ALL SELECT '2026-10-09','Sampdoria','Serie B 2026/27','CASA',4,1,90,3,2,'AMMONIZIONE','espn:53344837','Michele Besaggio - Avellino','Michele Besaggio'
    UNION ALL SELECT '2026-10-18','Cesena','Serie B 2026/27','TRASFERTA',1,1,13,0,1,'AMMONIZIONE','espn:53909063','Brando Moruzzi - Avellino','Brando Moruzzi'
    UNION ALL SELECT '2026-10-18','Cesena','Serie B 2026/27','TRASFERTA',1,1,45,0,2,'SOSTITUZIONE_IN','espn:53913642','Matteo Guidi entra, Riccardo Pagano esce - Cesena',NULL
    UNION ALL SELECT '2026-10-18','Cesena','Serie B 2026/27','TRASFERTA',1,1,51,0,2,'GOL','espn:53914071','Antonio Fiori - Cesena',NULL
    UNION ALL SELECT '2026-10-18','Cesena','Serie B 2026/27','TRASFERTA',1,1,57,0,2,'SOSTITUZIONE_IN','espn:53914488','Giacomo Faticanti entra, Luca Palmiero esce - Avellino','Giacomo Faticanti'
    UNION ALL SELECT '2026-10-18','Cesena','Serie B 2026/27','TRASFERTA',1,1,57,0,2,'SOSTITUZIONE_IN','espn:53914530','Luca Di Maggio entra, Martin Palumbo esce - Avellino','Luca Di Maggio'
    UNION ALL SELECT '2026-10-18','Cesena','Serie B 2026/27','TRASFERTA',1,1,64,0,2,'SOSTITUZIONE_IN','espn:53915171','Alessandro Debenedetti entra, Antonio Fiori esce - Cesena',NULL
    UNION ALL SELECT '2026-10-18','Cesena','Serie B 2026/27','TRASFERTA',1,1,65,0,2,'SOSTITUZIONE_IN','espn:53915274','Massimiliano Mangraviti entra, Luca D''Andrea esce - Cesena',NULL
    UNION ALL SELECT '2026-10-18','Cesena','Serie B 2026/27','TRASFERTA',1,1,67,0,2,'GOL','espn:53915506','Armando Izzo - Avellino','Armando Izzo'
    UNION ALL SELECT '2026-10-18','Cesena','Serie B 2026/27','TRASFERTA',1,1,73,0,2,'SOSTITUZIONE_IN','espn:53916218','Andrea Favilli entra, Walid Cheddira esce - Avellino','Andrea Favilli'
    UNION ALL SELECT '2026-10-18','Cesena','Serie B 2026/27','TRASFERTA',1,1,77,0,2,'SOSTITUZIONE_IN','espn:53916681','Matteo Piacentini entra, Mihajlo Ilic esce - Cesena',NULL
    UNION ALL SELECT '2026-10-18','Cesena','Serie B 2026/27','TRASFERTA',1,1,83,0,2,'AMMONIZIONE','espn:53917279','Emil Bohinen - Cesena',NULL
    UNION ALL SELECT '2026-10-18','Cesena','Serie B 2026/27','TRASFERTA',1,1,84,0,2,'SOSTITUZIONE_IN','espn:53917342','Dimitris Sounas entra, Michele Besaggio esce - Avellino','Dimitris Sounas'
    UNION ALL SELECT '2026-10-18','Cesena','Serie B 2026/27','TRASFERTA',1,1,84,0,2,'SOSTITUZIONE_IN','espn:53917357','Tommaso Biasci entra, Raffaele Russo esce - Avellino','Tommaso Biasci'
    UNION ALL SELECT '2026-10-18','Cesena','Serie B 2026/27','TRASFERTA',1,1,84,0,2,'SOSTITUZIONE_IN','espn:53917444','Matteo Francesconi entra, Emil Bohinen esce - Cesena',NULL
    UNION ALL SELECT '2026-10-24','Padova','Serie B 2026/27','CASA',0,1,20,0,1,'GOL','espn:54153983','Emanuele Zuelli - Padova',NULL
    UNION ALL SELECT '2026-10-24','Padova','Serie B 2026/27','CASA',0,1,41,0,1,'AMMONIZIONE','espn:54156401','Giovanni Giunti - Padova',NULL
    UNION ALL SELECT '2026-10-24','Padova','Serie B 2026/27','CASA',0,1,41,0,1,'AMMONIZIONE','espn:54156402','Pietro Fusi - Padova',NULL
    UNION ALL SELECT '2026-10-24','Padova','Serie B 2026/27','CASA',0,1,45,0,2,'SOSTITUZIONE_IN','espn:54159104','Luca Di Maggio entra, Luca Palmiero esce - Avellino','Luca Di Maggio'
    UNION ALL SELECT '2026-10-24','Padova','Serie B 2026/27','CASA',0,1,59,0,2,'SOSTITUZIONE_IN','espn:54162810','Andrea Favilli entra, Walid Cheddira esce - Avellino','Andrea Favilli'
    UNION ALL SELECT '2026-10-24','Padova','Serie B 2026/27','CASA',0,1,59,0,2,'SOSTITUZIONE_IN','espn:54162811','Armando Izzo entra, Alessandro Di Bitonto esce - Avellino','Armando Izzo'
    UNION ALL SELECT '2026-10-24','Padova','Serie B 2026/27','CASA',0,1,66,0,2,'SOSTITUZIONE_IN','espn:54164274','Kevin Varas Marcillo entra, Pietro Fusi esce - Padova',NULL
    UNION ALL SELECT '2026-10-24','Padova','Serie B 2026/27','CASA',0,1,74,0,2,'SOSTITUZIONE_IN','espn:54165978','Tommaso Biasci entra, Martin Palumbo esce - Avellino','Tommaso Biasci'
    UNION ALL SELECT '2026-10-24','Padova','Serie B 2026/27','CASA',0,1,77,0,2,'SOSTITUZIONE_IN','espn:54167219','Marco Pompetti entra, Giovanni Giunti esce - Padova',NULL
    UNION ALL SELECT '2026-10-24','Padova','Serie B 2026/27','CASA',0,1,77,0,2,'SOSTITUZIONE_IN','espn:54166896','Mattia Bortolussi entra, Gabriele Artistico esce - Padova',NULL
    UNION ALL SELECT '2026-10-24','Padova','Serie B 2026/27','CASA',0,1,82,0,2,'SOSTITUZIONE_IN','espn:54168251','Sebastiano Di Paolo entra, Brando Moruzzi esce - Avellino','Sebastiano Di Paolo'
    UNION ALL SELECT '2026-10-24','Padova','Serie B 2026/27','CASA',0,1,90,2,2,'SOSTITUZIONE_IN','espn:54170773','Alessandro Seghetti entra, Luca Moro esce - Padova',NULL
    UNION ALL SELECT '2026-10-24','Padova','Serie B 2026/27','CASA',0,1,90,2,2,'SOSTITUZIONE_IN','espn:54170774','Nicolas Galazzi entra, Emanuele Zuelli esce - Padova',NULL
    UNION ALL SELECT '2026-10-27','Virtus Entella','Serie B 2026/27','TRASFERTA',1,1,27,0,1,'GOL','espn:54387828','Mattia Tirelli - Virtus Entella',NULL
    UNION ALL SELECT '2026-10-27','Virtus Entella','Serie B 2026/27','TRASFERTA',1,1,45,0,1,'AMMONIZIONE','espn:54388550','Patrick Enrici - Avellino','Patrick Enrici'
    UNION ALL SELECT '2026-10-27','Virtus Entella','Serie B 2026/27','TRASFERTA',1,1,57,0,2,'AMMONIZIONE','espn:54391425','Mattia Alborghetti - Virtus Entella',NULL
    UNION ALL SELECT '2026-10-27','Virtus Entella','Serie B 2026/27','TRASFERTA',1,1,58,0,2,'SOSTITUZIONE_IN','espn:54391650','Martin Palumbo entra, Giacomo Faticanti esce - Avellino','Martin Palumbo'
    UNION ALL SELECT '2026-10-27','Virtus Entella','Serie B 2026/27','TRASFERTA',1,1,58,0,2,'SOSTITUZIONE_IN','espn:54391705','Raffaele Russo entra, Tommaso Biasci esce - Avellino','Raffaele Russo'
    UNION ALL SELECT '2026-10-27','Virtus Entella','Serie B 2026/27','TRASFERTA',1,1,58,0,2,'SOSTITUZIONE_IN','espn:54391652','Michele Besaggio entra, Dimitris Sounas esce - Avellino','Michele Besaggio'
    UNION ALL SELECT '2026-10-27','Virtus Entella','Serie B 2026/27','TRASFERTA',1,1,58,0,2,'SOSTITUZIONE_IN','espn:54391706','Francesco Mezzoni entra, Tommaso Annoni esce - Virtus Entella',NULL
    UNION ALL SELECT '2026-10-27','Virtus Entella','Serie B 2026/27','TRASFERTA',1,1,59,0,2,'SOSTITUZIONE_IN','espn:54391707','Bernat Guiu entra, Luigi Cuppone esce - Virtus Entella',NULL
    UNION ALL SELECT '2026-10-27','Virtus Entella','Serie B 2026/27','TRASFERTA',1,1,63,0,2,'AMMONIZIONE','espn:54392217','Andrea Franzoni - Virtus Entella',NULL
    UNION ALL SELECT '2026-10-27','Virtus Entella','Serie B 2026/27','TRASFERTA',1,1,67,0,2,'AMMONIZIONE','espn:54392761','Michael Venturi - Avellino','Michael Venturi'
    UNION ALL SELECT '2026-10-27','Virtus Entella','Serie B 2026/27','TRASFERTA',1,1,71,0,2,'SOSTITUZIONE_IN','espn:54393308','Emanuele Adamo entra, Filippo Missori esce - Avellino','Emanuele Adamo'
    UNION ALL SELECT '2026-10-27','Virtus Entella','Serie B 2026/27','TRASFERTA',1,1,72,0,2,'SOSTITUZIONE_IN','espn:54393402','Leonardo Benedetti entra, Mattia Tirelli esce - Virtus Entella',NULL
    UNION ALL SELECT '2026-10-27','Virtus Entella','Serie B 2026/27','TRASFERTA',1,1,82,0,2,'AMMONIZIONE','espn:54394420','Martin Palumbo - Avellino','Martin Palumbo'
    UNION ALL SELECT '2026-10-27','Virtus Entella','Serie B 2026/27','TRASFERTA',1,1,84,0,2,'SOSTITUZIONE_IN','espn:54394578','Luca Pandolfi entra, Luca Di Maggio esce - Avellino','Luca Pandolfi'
    UNION ALL SELECT '2026-10-27','Virtus Entella','Serie B 2026/27','TRASFERTA',1,1,89,0,2,'SOSTITUZIONE_IN','espn:54394727','Riccardo Turicchia entra, Niccolò Squizzato esce - Virtus Entella',NULL
    UNION ALL SELECT '2026-10-27','Virtus Entella','Serie B 2026/27','TRASFERTA',1,1,89,0,2,'SOSTITUZIONE_IN','espn:54394728','Giacomo Corona entra, Stefano Di Mario esce - Virtus Entella',NULL
    UNION ALL SELECT '2026-10-27','Virtus Entella','Serie B 2026/27','TRASFERTA',1,1,90,4,2,'GOL','espn:54395099','Emanuele Adamo - Avellino','Emanuele Adamo'
    UNION ALL SELECT '2026-11-01','Pisa','Serie B 2026/27','CASA',2,0,26,0,1,'GOL','espn:54641321','Luca Di Maggio - Avellino','Luca Di Maggio'
    UNION ALL SELECT '2026-11-01','Pisa','Serie B 2026/27','CASA',2,0,45,0,2,'SOSTITUZIONE_IN','espn:54645565','Rosen Bozhinov entra, Andrea Primasso esce - Pisa',NULL
    UNION ALL SELECT '2026-11-01','Pisa','Serie B 2026/27','CASA',2,0,45,0,2,'SOSTITUZIONE_IN','espn:54645566','Tommaso Marras entra, Simone Zanon esce - Pisa',NULL
    UNION ALL SELECT '2026-11-01','Pisa','Serie B 2026/27','CASA',2,0,59,0,2,'GOL','espn:54648167','Raffaele Russo - Avellino','Raffaele Russo'
    UNION ALL SELECT '2026-11-01','Pisa','Serie B 2026/27','CASA',2,0,69,0,2,'SOSTITUZIONE_IN','espn:54649180','Dimitris Sounas entra, Luca Di Maggio esce - Avellino','Dimitris Sounas'
    UNION ALL SELECT '2026-11-01','Pisa','Serie B 2026/27','CASA',2,0,69,0,2,'SOSTITUZIONE_IN','espn:54649231','Luca Pandolfi entra, Walid Cheddira esce - Avellino','Luca Pandolfi'
    UNION ALL SELECT '2026-11-01','Pisa','Serie B 2026/27','CASA',2,0,69,0,2,'SOSTITUZIONE_IN','espn:54649232','Matteo Tramoni entra, Emanuele Rao esce - Pisa',NULL
    UNION ALL SELECT '2026-11-01','Pisa','Serie B 2026/27','CASA',2,0,69,0,2,'SOSTITUZIONE_IN','espn:54649233','Felipe Loyola entra, Giuseppe Leone esce - Pisa',NULL
    UNION ALL SELECT '2026-11-01','Pisa','Serie B 2026/27','CASA',2,0,69,0,2,'SOSTITUZIONE_IN','espn:54649234','Simone Canestrelli entra, Arturo Calabresi esce - Pisa',NULL
    UNION ALL SELECT '2026-11-01','Pisa','Serie B 2026/27','CASA',2,0,83,0,2,'AMMONIZIONE','espn:54651339','Samuele Angori - Pisa',NULL
    UNION ALL SELECT '2026-11-01','Pisa','Serie B 2026/27','CASA',2,0,87,0,2,'SOSTITUZIONE_IN','espn:54651966','Tommaso Cancellotti entra, Armando Izzo esce - Avellino','Tommaso Cancellotti'
    UNION ALL SELECT '2026-11-01','Pisa','Serie B 2026/27','CASA',2,0,90,6,2,'SOSTITUZIONE_IN','espn:54653455','Andrea Favilli entra, Raffaele Russo esce - Avellino','Andrea Favilli'
    UNION ALL SELECT '2026-11-01','Pisa','Serie B 2026/27','CASA',2,0,90,7,2,'SOSTITUZIONE_IN','espn:54653456','Luca Palmiero entra, Michele Besaggio esce - Avellino','Luca Palmiero'
    UNION ALL SELECT '2026-11-07','Benevento','Serie B 2026/27','TRASFERTA',1,1,38,0,1,'SOSTITUZIONE_IN','espn:54984066','Raffaele Romano entra, Leonardo Sernicola esce - Benevento',NULL
    UNION ALL SELECT '2026-11-07','Benevento','Serie B 2026/27','TRASFERTA',1,1,42,0,1,'AMMONIZIONE','espn:54984559','Francesco Salvemini - Benevento',NULL
    UNION ALL SELECT '2026-11-07','Benevento','Serie B 2026/27','TRASFERTA',1,1,59,0,2,'AMMONIZIONE','espn:54989729','Martin Palumbo - Avellino','Martin Palumbo'
    UNION ALL SELECT '2026-11-07','Benevento','Serie B 2026/27','TRASFERTA',1,1,60,0,2,'GOL','espn:54989840','Antonis Siatounis - Benevento',NULL
    UNION ALL SELECT '2026-11-07','Benevento','Serie B 2026/27','TRASFERTA',1,1,65,0,2,'SOSTITUZIONE_IN','espn:54990143','Luca Palmiero entra, Michele Besaggio esce - Avellino','Luca Palmiero'
    UNION ALL SELECT '2026-11-07','Benevento','Serie B 2026/27','TRASFERTA',1,1,67,0,2,'GOL','espn:54990266','Walid Cheddira - Avellino','Walid Cheddira'
    UNION ALL SELECT '2026-11-07','Benevento','Serie B 2026/27','TRASFERTA',1,1,74,0,2,'SOSTITUZIONE_IN','espn:54990852','Dimitris Sounas entra, Martin Palumbo esce - Avellino','Dimitris Sounas'
    UNION ALL SELECT '2026-11-07','Benevento','Serie B 2026/27','TRASFERTA',1,1,75,0,2,'SOSTITUZIONE_IN','espn:54990987','Marco Tumminello entra, Francesco Salvemini esce - Benevento',NULL
    UNION ALL SELECT '2026-11-07','Benevento','Serie B 2026/27','TRASFERTA',1,1,75,0,2,'SOSTITUZIONE_IN','espn:54990934','Antonio Prisco entra, Mattia Maita esce - Benevento',NULL
    UNION ALL SELECT '2026-11-07','Benevento','Serie B 2026/27','TRASFERTA',1,1,75,0,2,'AMMONIZIONE','espn:54991003','Stefano Scognamillo - Benevento',NULL
    UNION ALL SELECT '2026-11-07','Benevento','Serie B 2026/27','TRASFERTA',1,1,81,0,2,'AMMONIZIONE','espn:54991526','Luca Di Maggio - Avellino','Luca Di Maggio'
    UNION ALL SELECT '2026-11-07','Benevento','Serie B 2026/27','TRASFERTA',1,1,84,0,2,'SOSTITUZIONE_IN','espn:54991843','Davide Lamesta entra, Luigi Cherubini esce - Benevento',NULL
    UNION ALL SELECT '2026-11-07','Benevento','Serie B 2026/27','TRASFERTA',1,1,84,0,2,'SOSTITUZIONE_IN','espn:54991809','Simone Verdi entra, David Okereke esce - Benevento',NULL
    UNION ALL SELECT '2026-11-07','Benevento','Serie B 2026/27','TRASFERTA',1,1,86,0,2,'SOSTITUZIONE_IN','espn:54992032','Emanuele Adamo entra, Luca Di Maggio esce - Avellino','Emanuele Adamo'
    UNION ALL SELECT '2026-11-07','Benevento','Serie B 2026/27','TRASFERTA',1,1,87,0,2,'SOSTITUZIONE_IN','espn:54992322','Tommaso Biasci entra, Raffaele Russo esce - Avellino','Tommaso Biasci'
    UNION ALL SELECT '2026-11-07','Benevento','Serie B 2026/27','TRASFERTA',1,1,87,0,2,'SOSTITUZIONE_IN','espn:54992220','Luca Pandolfi entra, Walid Cheddira esce - Avellino','Luca Pandolfi'
    UNION ALL SELECT '2026-11-07','Benevento','Serie B 2026/27','TRASFERTA',1,1,90,3,2,'AMMONIZIONE','espn:54992895','Pietro Beruatto - Benevento',NULL
) s
JOIN partite p ON p.data=s.data_partita AND p.avversario=s.avversario AND p.competizione=s.competizione
    AND p.casa_trasferta=s.casa AND p.gol_fatti=s.fatti AND p.gol_subiti=s.subiti
WHERE NOT EXISTS (SELECT 1 FROM eventi_partita e WHERE e.riferimento_esterno=s.riferimento);

COMMIT;