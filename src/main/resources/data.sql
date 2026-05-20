INSERT INTO statut (id, code, libelle, ordre_affichage) SELECT 1, 'CREATION', 'Dossier créé', 1 WHERE NOT EXISTS (SELECT 1 FROM statut WHERE id = 1);
INSERT INTO statut (id, code, libelle, ordre_affichage) SELECT 2, 'SCANNE', 'Dossier scanné', 2 WHERE NOT EXISTS (SELECT 1 FROM statut WHERE id = 2);
INSERT INTO statut (id, code, libelle, ordre_affichage) SELECT 3, 'TERMINE', 'Dossier terminé', 3 WHERE NOT EXISTS (SELECT 1 FROM statut WHERE id = 3);
INSERT INTO statut (id, code, libelle, ordre_affichage) SELECT 10, 'DUPLICATA_DEMANDE', 'Duplicata demandé', 10 WHERE NOT EXISTS (SELECT 1 FROM statut WHERE id = 10);
INSERT INTO statut (id, code, libelle, ordre_affichage) SELECT 11, 'DUPLICATA_SCANNE', 'Duplicata scanné', 11 WHERE NOT EXISTS (SELECT 1 FROM statut WHERE id = 11);
INSERT INTO statut (id, code, libelle, ordre_affichage) SELECT 12, 'DUPLICATA_VALIDE', 'Duplicata validé', 12 WHERE NOT EXISTS (SELECT 1 FROM statut WHERE id = 12);
INSERT INTO statut (id, code, libelle, ordre_affichage) SELECT 13, 'DUPLICATA_REJETE', 'Duplicata rejeté', 13 WHERE NOT EXISTS (SELECT 1 FROM statut WHERE id = 13);
INSERT INTO statut (id, code, libelle, ordre_affichage) SELECT 14, 'DUPLICATA_EMIS', 'Duplicata émis', 14 WHERE NOT EXISTS (SELECT 1 FROM statut WHERE id = 14);
INSERT INTO statut (id, code, libelle, ordre_affichage) SELECT 20, 'TRANSFERT_DEMANDE', 'Transfert demandé', 20 WHERE NOT EXISTS (SELECT 1 FROM statut WHERE id = 20);
INSERT INTO statut (id, code, libelle, ordre_affichage) SELECT 21, 'TRANSFERT_SCANNE', 'Transfert scanné', 21 WHERE NOT EXISTS (SELECT 1 FROM statut WHERE id = 21);
INSERT INTO statut (id, code, libelle, ordre_affichage) SELECT 22, 'TRANSFERT_VALIDE', 'Transfert validé', 22 WHERE NOT EXISTS (SELECT 1 FROM statut WHERE id = 22);
INSERT INTO statut (id, code, libelle, ordre_affichage) SELECT 23, 'TRANSFERT_REJETE', 'Transfert rejeté', 23 WHERE NOT EXISTS (SELECT 1 FROM statut WHERE id = 23);
INSERT INTO statut (id, code, libelle, ordre_affichage) SELECT 24, 'TRANSFERT_EMIS', 'Transfert émis', 24 WHERE NOT EXISTS (SELECT 1 FROM statut WHERE id = 24);

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
INSERT INTO type_piece_commune (libelle, obligatoire) SELECT 'Lettre de recommandation', FALSE WHERE NOT EXISTS (SELECT 1 FROM type_piece_commune WHERE libelle = 'Lettre de recommandation');

INSERT INTO type_piece_specifique (libelle, id_type_visa, obligatoire) SELECT 'Statut de la Société', 1, TRUE WHERE NOT EXISTS (SELECT 1 FROM type_piece_specifique WHERE libelle = 'Statut de la Société' AND id_type_visa = 1);
INSERT INTO type_piece_specifique (libelle, id_type_visa, obligatoire) SELECT 'Extrait d''inscription au registre de commerce', 1, TRUE WHERE NOT EXISTS (SELECT 1 FROM type_piece_specifique WHERE libelle = 'Extrait d''inscription au registre de commerce' AND id_type_visa = 1);
INSERT INTO type_piece_specifique (libelle, id_type_visa, obligatoire) SELECT 'Carte fiscale', 1, TRUE WHERE NOT EXISTS (SELECT 1 FROM type_piece_specifique WHERE libelle = 'Carte fiscale' AND id_type_visa = 1);
INSERT INTO type_piece_specifique (libelle, id_type_visa, obligatoire) SELECT 'Plan d''affaires (facultatif)', 1, FALSE WHERE NOT EXISTS (SELECT 1 FROM type_piece_specifique WHERE libelle = 'Plan d''affaires (facultatif)' AND id_type_visa = 1);
INSERT INTO type_piece_specifique (libelle, id_type_visa, obligatoire) SELECT 'Contrat de travail', 2, TRUE WHERE NOT EXISTS (SELECT 1 FROM type_piece_specifique WHERE libelle = 'Contrat de travail' AND id_type_visa = 2);
INSERT INTO type_piece_specifique (libelle, id_type_visa, obligatoire) SELECT 'Autorisation de travail', 2, TRUE WHERE NOT EXISTS (SELECT 1 FROM type_piece_specifique WHERE libelle = 'Autorisation de travail' AND id_type_visa = 2);
INSERT INTO type_piece_specifique (libelle, id_type_visa, obligatoire) SELECT 'Diplôme ou certificat professionnel (facultatif)', 2, FALSE WHERE NOT EXISTS (SELECT 1 FROM type_piece_specifique WHERE libelle = 'Diplôme ou certificat professionnel (facultatif)' AND id_type_visa = 2);
