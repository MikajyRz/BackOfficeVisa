-- =============================================
-- BASE DE DONNÃ‰ES VISA - PostgreSQL
-- =============================================

-- Suppression des donnÃ©es
DELETE FROM Piece_demande_specifique;
DELETE FROM Piece_demande;
DELETE FROM Capture_signature;
DELETE FROM Historique_passeport_visa;
DELETE FROM carte_resident;
DELETE FROM Visa;
DELETE FROM Statut_demande;
DELETE FROM Demande;
DELETE FROM Statut;
DELETE FROM Visa_transformable;
DELETE FROM Passeport;
DELETE FROM Demandeur;
DELETE FROM Type_piece_specifique;
DELETE FROM Type_piece_commune;
DELETE FROM Type_demande;
DELETE FROM type_visa;
DELETE FROM Situation_familiale;
DELETE FROM Nationalite;

-- Suppression des tables
DROP TABLE IF EXISTS Piece_demande_specifique;
DROP TABLE IF EXISTS Piece_demande;
DROP TABLE IF EXISTS Capture_signature;
DROP TABLE IF EXISTS Historique_passeport_visa;
DROP TABLE IF EXISTS carte_resident;
DROP TABLE IF EXISTS Visa;
DROP TABLE IF EXISTS Statut_demande;
DROP TABLE IF EXISTS Demande;
DROP TABLE IF EXISTS Statut;
DROP TABLE IF EXISTS Visa_transformable;
DROP TABLE IF EXISTS Passeport;
DROP TABLE IF EXISTS Demandeur;
DROP TABLE IF EXISTS Type_piece_specifique;
DROP TABLE IF EXISTS Type_piece_commune;
DROP TABLE IF EXISTS Type_demande;
DROP TABLE IF EXISTS type_visa;
DROP TABLE IF EXISTS Situation_familiale;
DROP TABLE IF EXISTS Nationalite;

-- =============================================

CREATE TABLE Nationalite (
    id SERIAL PRIMARY KEY,
    libelle VARCHAR(50) NOT NULL
);

CREATE TABLE Situation_familiale (
    id SERIAL PRIMARY KEY,
    libelle VARCHAR(50) NOT NULL
);

CREATE TABLE type_visa (
    id SERIAL PRIMARY KEY,
    libelle VARCHAR(50) NOT NULL
);

CREATE TABLE Type_demande (
    id SERIAL PRIMARY KEY,
    libelle VARCHAR(50) NOT NULL
);

CREATE TABLE Statut (
    id INT PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    libelle VARCHAR(100) NOT NULL,
    ordre_affichage INT NOT NULL
);

CREATE TABLE Demandeur (
    id SERIAL PRIMARY KEY,
    nom VARCHAR(50) NOT NULL,
    prenom VARCHAR(50) NOT NULL,
    date_naissance DATE NOT NULL,
    lieu_naissance VARCHAR(100) NOT NULL,
    telephone VARCHAR(20) NOT NULL,
    email VARCHAR(100) NOT NULL,
    adresse TEXT NOT NULL,
    id_situation_familiale INT NOT NULL,
    id_nationalite INT NOT NULL,
    FOREIGN KEY (id_situation_familiale) REFERENCES Situation_familiale(id),
    FOREIGN KEY (id_nationalite) REFERENCES Nationalite(id)
);

CREATE TABLE Passeport (
    id SERIAL PRIMARY KEY,
    id_demandeur INT NOT NULL,
    numero_passeport VARCHAR(50) NOT NULL UNIQUE,
    date_delivrance DATE NOT NULL,
    date_expiration DATE NOT NULL,
    pays_delivrance VARCHAR(100),
    FOREIGN KEY (id_demandeur) REFERENCES Demandeur(id)
);

CREATE TABLE Visa_transformable (
    id SERIAL PRIMARY KEY,
    id_demandeur INT NOT NULL,
    id_passeport INT NOT NULL,
    numero_reference VARCHAR(50) NOT NULL UNIQUE,
    lieu VARCHAR(100) NOT NULL,
    date_debut DATE NOT NULL,
    date_fin DATE NOT NULL,
    FOREIGN KEY (id_demandeur) REFERENCES Demandeur(id),
    FOREIGN KEY (id_passeport) REFERENCES Passeport(id)
);

CREATE TABLE Demande (
    id SERIAL PRIMARY KEY,
    id_visa_transformable INT,
    date_demande DATE NOT NULL,
    id_statut INT NOT NULL DEFAULT 1,
    id_demandeur INT NOT NULL,
    id_type_visa INT NOT NULL,
    id_type_demande INT NOT NULL,
    date_traitement DATE,
    FOREIGN KEY (id_type_demande) REFERENCES Type_demande(id),
    FOREIGN KEY (id_demandeur) REFERENCES Demandeur(id),
    FOREIGN KEY (id_type_visa) REFERENCES type_visa(id),
    FOREIGN KEY (id_statut) REFERENCES Statut(id),
    FOREIGN KEY (id_visa_transformable) REFERENCES Visa_transformable(id)
);

CREATE TABLE Statut_demande (
    id SERIAL PRIMARY KEY,
    id_demande INT NOT NULL,
    statut INT NOT NULL,
    date_changement_statut DATE,
    FOREIGN KEY (id_demande) REFERENCES Demande(id),
    FOREIGN KEY (statut) REFERENCES Statut(id)
);

CREATE TABLE Capture_signature (
    id SERIAL PRIMARY KEY,
    id_demande INT NOT NULL UNIQUE,
    photo_path VARCHAR(255) NOT NULL,
    signature_path VARCHAR(255) NOT NULL,
    date_capture TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (id_demande) REFERENCES Demande(id)
);

CREATE TABLE Type_piece_commune (
    id SERIAL PRIMARY KEY,
    libelle VARCHAR(150) NOT NULL,
    obligatoire BOOLEAN DEFAULT TRUE
);

CREATE TABLE Type_piece_specifique (
    id SERIAL PRIMARY KEY,
    libelle VARCHAR(150) NOT NULL,
    id_type_visa INT NOT NULL,
    obligatoire BOOLEAN DEFAULT TRUE,
    FOREIGN KEY (id_type_visa) REFERENCES type_visa(id)
);

CREATE TABLE Piece_demande (
    id SERIAL PRIMARY KEY,
    id_demande INT NOT NULL,
    id_type_piece_commune INT NOT NULL,
    presente BOOLEAN DEFAULT FALSE,
    fichier_path VARCHAR(255),
    FOREIGN KEY (id_demande) REFERENCES Demande(id),
    FOREIGN KEY (id_type_piece_commune) REFERENCES Type_piece_commune(id),
    UNIQUE (id_demande, id_type_piece_commune)
);

CREATE TABLE Piece_demande_specifique (
    id SERIAL PRIMARY KEY,
    id_demande INT NOT NULL,
    id_type_piece INT NOT NULL,
    presente BOOLEAN DEFAULT FALSE,
    fichier_path VARCHAR(255),
    FOREIGN KEY (id_demande) REFERENCES Demande(id),
    FOREIGN KEY (id_type_piece) REFERENCES Type_piece_specifique(id),
    UNIQUE (id_demande, id_type_piece)
);

CREATE TABLE Visa (
    id SERIAL PRIMARY KEY,
    id_demande INT NOT NULL,
    reference VARCHAR(50),
    date_debut DATE NOT NULL,
    date_fin DATE NOT NULL,
    id_passeport INT NOT NULL,
    FOREIGN KEY (id_passeport) REFERENCES Passeport(id),
    FOREIGN KEY (id_demande) REFERENCES Demande(id)
);

CREATE TABLE carte_resident (
    id SERIAL PRIMARY KEY,
    id_demande INT NOT NULL,
    reference VARCHAR(50),
    date_debut DATE NOT NULL,
    date_fin DATE NOT NULL,
    id_passeport INT NOT NULL,
    FOREIGN KEY (id_demande) REFERENCES Demande(id),
    FOREIGN KEY (id_passeport) REFERENCES Passeport(id)
);

CREATE TABLE Historique_passeport_visa (
    id SERIAL PRIMARY KEY,
    id_visa_transformable INT NOT NULL,
    id_ancien_passeport INT,
    id_nouveau_passeport INT NOT NULL,
    date_transfert DATE NOT NULL,
    FOREIGN KEY (id_visa_transformable) REFERENCES Visa_transformable(id),
    FOREIGN KEY (id_ancien_passeport) REFERENCES Passeport(id),
    FOREIGN KEY (id_nouveau_passeport) REFERENCES Passeport(id)
);

-- Index pour les recherches frÃ©quentes
CREATE INDEX idx_demande_statut ON Demande(id_statut);
CREATE INDEX idx_demande_demandeur ON Demande(id_demandeur);

-- =============================================
-- DONNÃ‰ES DE RÃ‰FÃ‰RENCE
-- =============================================

INSERT INTO Nationalite (libelle) VALUES ('FranÃ§aise'), ('Malgache'), ('Chinoise'), ('Indienne'), ('Comorienne'), ('Autre');

INSERT INTO Situation_familiale (libelle) VALUES ('CÃ©libataire'), ('MariÃ©(e)'), ('DivorcÃ©(e)'), ('Veuf/Veuve');

INSERT INTO type_visa (libelle) VALUES ('Investisseur'), ('Travailleur');

INSERT INTO Type_demande (libelle) VALUES ('Nouveau titre'), ('Duplicata'), ('Transfert de visa');

INSERT INTO Statut (id, code, libelle, ordre_affichage) VALUES
(1, 'CREATION', 'Dossier cree', 1),
(2, 'SIGNATURE_TERMINEE', 'Signature terminee', 2),
(3, 'SCANNE', 'Dossier scanne', 3),
(4, 'TERMINE', 'Dossier termine', 4),
(10, 'DUPLICATA_DEMANDE', 'Duplicata demande', 10),
(11, 'DUPLICATA_SIGNATURE_TERMINEE', 'Duplicata signature terminee', 11),
(12, 'DUPLICATA_SCANNE', 'Duplicata scanne', 12),
(13, 'DUPLICATA_VALIDE', 'Duplicata valide', 13),
(14, 'DUPLICATA_REJETE', 'Duplicata rejete', 14),
(15, 'DUPLICATA_EMIS', 'Duplicata emis', 15),
(20, 'TRANSFERT_DEMANDE', 'Transfert demande', 20),
(21, 'TRANSFERT_SIGNATURE_TERMINEE', 'Transfert signature terminee', 21),
(22, 'TRANSFERT_SCANNE', 'Transfert scanne', 22),
(23, 'TRANSFERT_VALIDE', 'Transfert valide', 23),
(24, 'TRANSFERT_REJETE', 'Transfert rejete', 24),
(25, 'TRANSFERT_EMIS', 'Transfert emis', 25);

INSERT INTO Type_piece_commune (libelle, obligatoire) VALUES
('02 photos d''identitÃ© rÃ©centes', TRUE),
('Notice de renseignement', TRUE),
('Demande Ã©crite adressÃ©e au MinistÃ¨re de l''IntÃ©rieur', TRUE),
('Photocopie certifiÃ©e de la premiÃ¨re page du passeport', TRUE),
('Certificat de rÃ©sidence ou attestation d''hÃ©bergement', TRUE),
('Extrait de casier judiciaire (< 3 mois)', TRUE);

INSERT INTO Type_piece_specifique (libelle, id_type_visa, obligatoire) VALUES
('Statut de la SociÃ©tÃ©', 1, TRUE),
('Extrait d''inscription au registre de commerce', 1, TRUE),
('Carte fiscale', 1, TRUE),
('Contrat de travail', 2, TRUE),
('Autorisation de travail', 2, TRUE);


