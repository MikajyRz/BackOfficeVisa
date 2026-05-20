INSERT INTO statut (id, code, libelle, ordre_affichage) VALUES (4, 'TERMINE_MIGRATION', 'Dossier termine', 4) ON CONFLICT (id) DO NOTHING;
INSERT INTO statut (id, code, libelle, ordre_affichage) VALUES (15, 'DUPLICATA_EMIS_MIGRATION', 'Duplicata emis', 15) ON CONFLICT (id) DO NOTHING;
INSERT INTO statut (id, code, libelle, ordre_affichage) VALUES (25, 'TRANSFERT_EMIS_MIGRATION', 'Transfert emis', 25) ON CONFLICT (id) DO NOTHING;

UPDATE demande SET id_statut = 4 WHERE id_statut = 3 AND EXISTS (SELECT 1 FROM statut WHERE id = 3 AND code = 'TERMINE');
UPDATE statut_demande SET statut = 4 WHERE statut = 3 AND EXISTS (SELECT 1 FROM statut WHERE id = 3 AND code = 'TERMINE');
UPDATE demande SET id_statut = 3 WHERE id_statut = 2 AND EXISTS (SELECT 1 FROM statut WHERE id = 2 AND code = 'SCANNE');
UPDATE statut_demande SET statut = 3 WHERE statut = 2 AND EXISTS (SELECT 1 FROM statut WHERE id = 2 AND code = 'SCANNE');

UPDATE demande SET id_statut = 15 WHERE id_statut = 14 AND EXISTS (SELECT 1 FROM statut WHERE id = 14 AND code = 'DUPLICATA_EMIS');
UPDATE statut_demande SET statut = 15 WHERE statut = 14 AND EXISTS (SELECT 1 FROM statut WHERE id = 14 AND code = 'DUPLICATA_EMIS');
UPDATE demande SET id_statut = 14 WHERE id_statut = 13 AND EXISTS (SELECT 1 FROM statut WHERE id = 13 AND code = 'DUPLICATA_REJETE');
UPDATE statut_demande SET statut = 14 WHERE statut = 13 AND EXISTS (SELECT 1 FROM statut WHERE id = 13 AND code = 'DUPLICATA_REJETE');
UPDATE demande SET id_statut = 13 WHERE id_statut = 12 AND EXISTS (SELECT 1 FROM statut WHERE id = 12 AND code = 'DUPLICATA_VALIDE');
UPDATE statut_demande SET statut = 13 WHERE statut = 12 AND EXISTS (SELECT 1 FROM statut WHERE id = 12 AND code = 'DUPLICATA_VALIDE');
UPDATE demande SET id_statut = 12 WHERE id_statut = 11 AND EXISTS (SELECT 1 FROM statut WHERE id = 11 AND code = 'DUPLICATA_SCANNE');
UPDATE statut_demande SET statut = 12 WHERE statut = 11 AND EXISTS (SELECT 1 FROM statut WHERE id = 11 AND code = 'DUPLICATA_SCANNE');

UPDATE demande SET id_statut = 25 WHERE id_statut = 24 AND EXISTS (SELECT 1 FROM statut WHERE id = 24 AND code = 'TRANSFERT_EMIS');
UPDATE statut_demande SET statut = 25 WHERE statut = 24 AND EXISTS (SELECT 1 FROM statut WHERE id = 24 AND code = 'TRANSFERT_EMIS');
UPDATE demande SET id_statut = 24 WHERE id_statut = 23 AND EXISTS (SELECT 1 FROM statut WHERE id = 23 AND code = 'TRANSFERT_REJETE');
UPDATE statut_demande SET statut = 24 WHERE statut = 23 AND EXISTS (SELECT 1 FROM statut WHERE id = 23 AND code = 'TRANSFERT_REJETE');
UPDATE demande SET id_statut = 23 WHERE id_statut = 22 AND EXISTS (SELECT 1 FROM statut WHERE id = 22 AND code = 'TRANSFERT_VALIDE');
UPDATE statut_demande SET statut = 23 WHERE statut = 22 AND EXISTS (SELECT 1 FROM statut WHERE id = 22 AND code = 'TRANSFERT_VALIDE');
UPDATE demande SET id_statut = 22 WHERE id_statut = 21 AND EXISTS (SELECT 1 FROM statut WHERE id = 21 AND code = 'TRANSFERT_SCANNE');
UPDATE statut_demande SET statut = 22 WHERE statut = 21 AND EXISTS (SELECT 1 FROM statut WHERE id = 21 AND code = 'TRANSFERT_SCANNE');

INSERT INTO statut (id, code, libelle, ordre_affichage) VALUES (1, 'CREATION', 'Dossier cree', 1) ON CONFLICT (id) DO UPDATE SET code = EXCLUDED.code, libelle = EXCLUDED.libelle, ordre_affichage = EXCLUDED.ordre_affichage;
INSERT INTO statut (id, code, libelle, ordre_affichage) VALUES (2, 'PHOTO_PRISE', 'Photo prise', 2) ON CONFLICT (id) DO UPDATE SET code = EXCLUDED.code, libelle = EXCLUDED.libelle, ordre_affichage = EXCLUDED.ordre_affichage;
INSERT INTO statut (id, code, libelle, ordre_affichage) VALUES (3, 'SCANNE', 'Dossier scanne', 3) ON CONFLICT (id) DO UPDATE SET code = EXCLUDED.code, libelle = EXCLUDED.libelle, ordre_affichage = EXCLUDED.ordre_affichage;
INSERT INTO statut (id, code, libelle, ordre_affichage) VALUES (4, 'TERMINE', 'Dossier termine', 4) ON CONFLICT (id) DO UPDATE SET code = EXCLUDED.code, libelle = EXCLUDED.libelle, ordre_affichage = EXCLUDED.ordre_affichage;
INSERT INTO statut (id, code, libelle, ordre_affichage) VALUES (10, 'DUPLICATA_DEMANDE', 'Duplicata demande', 10) ON CONFLICT (id) DO UPDATE SET code = EXCLUDED.code, libelle = EXCLUDED.libelle, ordre_affichage = EXCLUDED.ordre_affichage;
INSERT INTO statut (id, code, libelle, ordre_affichage) VALUES (11, 'DUPLICATA_SIGNATURE_TERMINEE', 'Duplicata signature terminee', 11) ON CONFLICT (id) DO UPDATE SET code = EXCLUDED.code, libelle = EXCLUDED.libelle, ordre_affichage = EXCLUDED.ordre_affichage;
INSERT INTO statut (id, code, libelle, ordre_affichage) VALUES (12, 'DUPLICATA_SCANNE', 'Duplicata scanne', 12) ON CONFLICT (id) DO UPDATE SET code = EXCLUDED.code, libelle = EXCLUDED.libelle, ordre_affichage = EXCLUDED.ordre_affichage;
INSERT INTO statut (id, code, libelle, ordre_affichage) VALUES (13, 'DUPLICATA_VALIDE', 'Duplicata valide', 13) ON CONFLICT (id) DO UPDATE SET code = EXCLUDED.code, libelle = EXCLUDED.libelle, ordre_affichage = EXCLUDED.ordre_affichage;
INSERT INTO statut (id, code, libelle, ordre_affichage) VALUES (14, 'DUPLICATA_REJETE', 'Duplicata rejete', 14) ON CONFLICT (id) DO UPDATE SET code = EXCLUDED.code, libelle = EXCLUDED.libelle, ordre_affichage = EXCLUDED.ordre_affichage;
INSERT INTO statut (id, code, libelle, ordre_affichage) VALUES (15, 'DUPLICATA_EMIS', 'Duplicata emis', 15) ON CONFLICT (id) DO UPDATE SET code = EXCLUDED.code, libelle = EXCLUDED.libelle, ordre_affichage = EXCLUDED.ordre_affichage;
INSERT INTO statut (id, code, libelle, ordre_affichage) VALUES (20, 'TRANSFERT_DEMANDE', 'Transfert demande', 20) ON CONFLICT (id) DO UPDATE SET code = EXCLUDED.code, libelle = EXCLUDED.libelle, ordre_affichage = EXCLUDED.ordre_affichage;
INSERT INTO statut (id, code, libelle, ordre_affichage) VALUES (21, 'TRANSFERT_SIGNATURE_TERMINEE', 'Transfert signature terminee', 21) ON CONFLICT (id) DO UPDATE SET code = EXCLUDED.code, libelle = EXCLUDED.libelle, ordre_affichage = EXCLUDED.ordre_affichage;
INSERT INTO statut (id, code, libelle, ordre_affichage) VALUES (22, 'TRANSFERT_SCANNE', 'Transfert scanne', 22) ON CONFLICT (id) DO UPDATE SET code = EXCLUDED.code, libelle = EXCLUDED.libelle, ordre_affichage = EXCLUDED.ordre_affichage;
INSERT INTO statut (id, code, libelle, ordre_affichage) VALUES (23, 'TRANSFERT_VALIDE', 'Transfert valide', 23) ON CONFLICT (id) DO UPDATE SET code = EXCLUDED.code, libelle = EXCLUDED.libelle, ordre_affichage = EXCLUDED.ordre_affichage;
INSERT INTO statut (id, code, libelle, ordre_affichage) VALUES (24, 'TRANSFERT_REJETE', 'Transfert rejete', 24) ON CONFLICT (id) DO UPDATE SET code = EXCLUDED.code, libelle = EXCLUDED.libelle, ordre_affichage = EXCLUDED.ordre_affichage;
INSERT INTO statut (id, code, libelle, ordre_affichage) VALUES (25, 'TRANSFERT_EMIS', 'Transfert emis', 25) ON CONFLICT (id) DO UPDATE SET code = EXCLUDED.code, libelle = EXCLUDED.libelle, ordre_affichage = EXCLUDED.ordre_affichage;


INSERT INTO nationalite (libelle) SELECT 'FranÃ§aise' WHERE NOT EXISTS (SELECT 1 FROM nationalite WHERE libelle = 'FranÃ§aise');
INSERT INTO nationalite (libelle) SELECT 'Malgache' WHERE NOT EXISTS (SELECT 1 FROM nationalite WHERE libelle = 'Malgache');
INSERT INTO nationalite (libelle) SELECT 'Chinoise' WHERE NOT EXISTS (SELECT 1 FROM nationalite WHERE libelle = 'Chinoise');
INSERT INTO nationalite (libelle) SELECT 'Indienne' WHERE NOT EXISTS (SELECT 1 FROM nationalite WHERE libelle = 'Indienne');
INSERT INTO nationalite (libelle) SELECT 'Comorienne' WHERE NOT EXISTS (SELECT 1 FROM nationalite WHERE libelle = 'Comorienne');
INSERT INTO nationalite (libelle) SELECT 'Autre' WHERE NOT EXISTS (SELECT 1 FROM nationalite WHERE libelle = 'Autre');

INSERT INTO situation_familiale (libelle) SELECT 'CÃ©libataire' WHERE NOT EXISTS (SELECT 1 FROM situation_familiale WHERE libelle = 'CÃ©libataire');
INSERT INTO situation_familiale (libelle) SELECT 'MariÃ©(e)' WHERE NOT EXISTS (SELECT 1 FROM situation_familiale WHERE libelle = 'MariÃ©(e)');
INSERT INTO situation_familiale (libelle) SELECT 'DivorcÃ©(e)' WHERE NOT EXISTS (SELECT 1 FROM situation_familiale WHERE libelle = 'DivorcÃ©(e)');
INSERT INTO situation_familiale (libelle) SELECT 'Veuf/Veuve' WHERE NOT EXISTS (SELECT 1 FROM situation_familiale WHERE libelle = 'Veuf/Veuve');

INSERT INTO type_visa (libelle) SELECT 'Investisseur' WHERE NOT EXISTS (SELECT 1 FROM type_visa WHERE libelle = 'Investisseur');
INSERT INTO type_visa (libelle) SELECT 'Travailleur' WHERE NOT EXISTS (SELECT 1 FROM type_visa WHERE libelle = 'Travailleur');

INSERT INTO type_demande (libelle) SELECT 'Nouveau titre' WHERE NOT EXISTS (SELECT 1 FROM type_demande WHERE libelle = 'Nouveau titre');
INSERT INTO type_demande (libelle) SELECT 'Duplicata' WHERE NOT EXISTS (SELECT 1 FROM type_demande WHERE libelle = 'Duplicata');
INSERT INTO type_demande (libelle) SELECT 'Transfert de visa' WHERE NOT EXISTS (SELECT 1 FROM type_demande WHERE libelle = 'Transfert de visa');

INSERT INTO type_piece_commune (libelle, obligatoire) SELECT '02 photos d''identitÃ© rÃ©centes', TRUE WHERE NOT EXISTS (SELECT 1 FROM type_piece_commune WHERE libelle = '02 photos d''identitÃ© rÃ©centes');
INSERT INTO type_piece_commune (libelle, obligatoire) SELECT 'Notice de renseignement', TRUE WHERE NOT EXISTS (SELECT 1 FROM type_piece_commune WHERE libelle = 'Notice de renseignement');
INSERT INTO type_piece_commune (libelle, obligatoire) SELECT 'Demande Ã©crite adressÃ©e au MinistÃ¨re de l''IntÃ©rieur', TRUE WHERE NOT EXISTS (SELECT 1 FROM type_piece_commune WHERE libelle = 'Demande Ã©crite adressÃ©e au MinistÃ¨re de l''IntÃ©rieur');
INSERT INTO type_piece_commune (libelle, obligatoire) SELECT 'Photocopie certifiÃ©e de la premiÃ¨re page du passeport', TRUE WHERE NOT EXISTS (SELECT 1 FROM type_piece_commune WHERE libelle = 'Photocopie certifiÃ©e de la premiÃ¨re page du passeport');
INSERT INTO type_piece_commune (libelle, obligatoire) SELECT 'Certificat de rÃ©sidence ou attestation d''hÃ©bergement', TRUE WHERE NOT EXISTS (SELECT 1 FROM type_piece_commune WHERE libelle = 'Certificat de rÃ©sidence ou attestation d''hÃ©bergement');
INSERT INTO type_piece_commune (libelle, obligatoire) SELECT 'Extrait de casier judiciaire (< 3 mois)', TRUE WHERE NOT EXISTS (SELECT 1 FROM type_piece_commune WHERE libelle = 'Extrait de casier judiciaire (< 3 mois)');
INSERT INTO type_piece_commune (libelle, obligatoire) SELECT 'Lettre de recommandation', FALSE WHERE NOT EXISTS (SELECT 1 FROM type_piece_commune WHERE libelle = 'Lettre de recommandation');

INSERT INTO type_piece_specifique (libelle, id_type_visa, obligatoire) SELECT 'Statut de la SociÃ©tÃ©', 1, TRUE WHERE NOT EXISTS (SELECT 1 FROM type_piece_specifique WHERE libelle = 'Statut de la SociÃ©tÃ©' AND id_type_visa = 1);
INSERT INTO type_piece_specifique (libelle, id_type_visa, obligatoire) SELECT 'Extrait d''inscription au registre de commerce', 1, TRUE WHERE NOT EXISTS (SELECT 1 FROM type_piece_specifique WHERE libelle = 'Extrait d''inscription au registre de commerce' AND id_type_visa = 1);
INSERT INTO type_piece_specifique (libelle, id_type_visa, obligatoire) SELECT 'Carte fiscale', 1, TRUE WHERE NOT EXISTS (SELECT 1 FROM type_piece_specifique WHERE libelle = 'Carte fiscale' AND id_type_visa = 1);
INSERT INTO type_piece_specifique (libelle, id_type_visa, obligatoire) SELECT 'Plan d''affaires (facultatif)', 1, FALSE WHERE NOT EXISTS (SELECT 1 FROM type_piece_specifique WHERE libelle = 'Plan d''affaires (facultatif)' AND id_type_visa = 1);
INSERT INTO type_piece_specifique (libelle, id_type_visa, obligatoire) SELECT 'Contrat de travail', 2, TRUE WHERE NOT EXISTS (SELECT 1 FROM type_piece_specifique WHERE libelle = 'Contrat de travail' AND id_type_visa = 2);
INSERT INTO type_piece_specifique (libelle, id_type_visa, obligatoire) SELECT 'Autorisation de travail', 2, TRUE WHERE NOT EXISTS (SELECT 1 FROM type_piece_specifique WHERE libelle = 'Autorisation de travail' AND id_type_visa = 2);
INSERT INTO type_piece_specifique (libelle, id_type_visa, obligatoire) SELECT 'DiplÃ´me ou certificat professionnel (facultatif)', 2, FALSE WHERE NOT EXISTS (SELECT 1 FROM type_piece_specifique WHERE libelle = 'DiplÃ´me ou certificat professionnel (facultatif)' AND id_type_visa = 2);
