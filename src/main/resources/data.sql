INSERT INTO nationalite (libelle) SELECT 'Française' WHERE NOT EXISTS (SELECT 1 FROM nationalite WHERE libelle = 'Française');
INSERT INTO nationalite (libelle) SELECT 'Malgache' WHERE NOT EXISTS (SELECT 1 FROM nationalite WHERE libelle = 'Malgache');
INSERT INTO nationalite (libelle) SELECT 'Chinoise' WHERE NOT EXISTS (SELECT 1 FROM nationalite WHERE libelle = 'Chinoise');
INSERT INTO nationalite (libelle) SELECT 'Indienne' WHERE NOT EXISTS (SELECT 1 FROM nationalite WHERE libelle = 'Indienne');
INSERT INTO nationalite (libelle) SELECT 'Comorienne' WHERE NOT EXISTS (SELECT 1 FROM nationalite WHERE libelle = 'Comorienne');
INSERT INTO nationalite (libelle) SELECT 'Autre' WHERE NOT EXISTS (SELECT 1 FROM nationalite WHERE libelle = 'Autre');

INSERT INTO situation_familiale (libelle) SELECT 'Célibataire' WHERE NOT EXISTS (SELECT 1 FROM situation_familiale WHERE libelle = 'Célibataire');
INSERT INTO situation_familiale (libelle) SELECT 'Marié(e)' WHERE NOT EXISTS (SELECT 1 FROM situation_familiale WHERE libelle = 'Marié(e)');
INSERT INTO situation_familiale (libelle) SELECT 'Divorcé(e)' WHERE NOT EXISTS (SELECT 1 FROM situation_familiale WHERE libelle = 'Divorcé(e)');
INSERT INTO situation_familiale (libelle) SELECT 'Veuf/Veuve' WHERE NOT EXISTS (SELECT 1 FROM situation_familiale WHERE libelle = 'Veuf/Veuve');

INSERT INTO type_visa (libelle) SELECT 'Investisseur' WHERE NOT EXISTS (SELECT 1 FROM type_visa WHERE libelle = 'Investisseur');
INSERT INTO type_visa (libelle) SELECT 'Travailleur' WHERE NOT EXISTS (SELECT 1 FROM type_visa WHERE libelle = 'Travailleur');

INSERT INTO type_demande (libelle) SELECT 'Nouveau titre' WHERE NOT EXISTS (SELECT 1 FROM type_demande WHERE libelle = 'Nouveau titre');
INSERT INTO type_demande (libelle) SELECT 'Duplicata' WHERE NOT EXISTS (SELECT 1 FROM type_demande WHERE libelle = 'Duplicata');
INSERT INTO type_demande (libelle) SELECT 'Transfert de visa' WHERE NOT EXISTS (SELECT 1 FROM type_demande WHERE libelle = 'Transfert de visa');

INSERT INTO type_piece_commune (libelle, obligatoire) SELECT '02 photos d''identité récentes', TRUE WHERE NOT EXISTS (SELECT 1 FROM type_piece_commune WHERE libelle = '02 photos d''identité récentes');
INSERT INTO type_piece_commune (libelle, obligatoire) SELECT 'Notice de renseignement', TRUE WHERE NOT EXISTS (SELECT 1 FROM type_piece_commune WHERE libelle = 'Notice de renseignement');
INSERT INTO type_piece_commune (libelle, obligatoire) SELECT 'Demande écrite adressée au Ministère de l''Intérieur', TRUE WHERE NOT EXISTS (SELECT 1 FROM type_piece_commune WHERE libelle = 'Demande écrite adressée au Ministère de l''Intérieur');
INSERT INTO type_piece_commune (libelle, obligatoire) SELECT 'Photocopie certifiée de la première page du passeport', TRUE WHERE NOT EXISTS (SELECT 1 FROM type_piece_commune WHERE libelle = 'Photocopie certifiée de la première page du passeport');
INSERT INTO type_piece_commune (libelle, obligatoire) SELECT 'Certificat de résidence ou attestation d''hébergement', TRUE WHERE NOT EXISTS (SELECT 1 FROM type_piece_commune WHERE libelle = 'Certificat de résidence ou attestation d''hébergement');
INSERT INTO type_piece_commune (libelle, obligatoire) SELECT 'Extrait de casier judiciaire (< 3 mois)', TRUE WHERE NOT EXISTS (SELECT 1 FROM type_piece_commune WHERE libelle = 'Extrait de casier judiciaire (< 3 mois)');

INSERT INTO type_piece_specifique (libelle, id_type_visa, obligatoire) SELECT 'Statut de la Société', 1, TRUE WHERE NOT EXISTS (SELECT 1 FROM type_piece_specifique WHERE libelle = 'Statut de la Société' AND id_type_visa = 1);
INSERT INTO type_piece_specifique (libelle, id_type_visa, obligatoire) SELECT 'Extrait d''inscription au registre de commerce', 1, TRUE WHERE NOT EXISTS (SELECT 1 FROM type_piece_specifique WHERE libelle = 'Extrait d''inscription au registre de commerce' AND id_type_visa = 1);
INSERT INTO type_piece_specifique (libelle, id_type_visa, obligatoire) SELECT 'Carte fiscale', 1, TRUE WHERE NOT EXISTS (SELECT 1 FROM type_piece_specifique WHERE libelle = 'Carte fiscale' AND id_type_visa = 1);
INSERT INTO type_piece_specifique (libelle, id_type_visa, obligatoire) SELECT 'Contrat de travail', 2, TRUE WHERE NOT EXISTS (SELECT 1 FROM type_piece_specifique WHERE libelle = 'Contrat de travail' AND id_type_visa = 2);
INSERT INTO type_piece_specifique (libelle, id_type_visa, obligatoire) SELECT 'Autorisation de travail', 2, TRUE WHERE NOT EXISTS (SELECT 1 FROM type_piece_specifique WHERE libelle = 'Autorisation de travail' AND id_type_visa = 2);
INSERT INTO type_piece_specifique (libelle, id_type_visa, obligatoire) SELECT 'Lettre de motivation', 1, FALSE WHERE NOT EXISTS (SELECT 1 FROM type_piece_specifique WHERE libelle = 'Lettre de motivation' AND id_type_visa = 1);
INSERT INTO type_piece_specifique (libelle, id_type_visa, obligatoire) SELECT 'Lettre de motivation', 2, FALSE WHERE NOT EXISTS (SELECT 1 FROM type_piece_specifique WHERE libelle = 'Lettre de motivation' AND id_type_visa = 2);
