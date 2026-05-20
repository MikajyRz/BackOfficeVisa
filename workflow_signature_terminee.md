# Fonctionnalite : etape "Signature terminee"

## Objectif

Ajouter une etape obligatoire entre la creation d'une demande et le scan termine.

Cette etape permet de capturer :

- une photo du demandeur via webcam ;
- une signature manuscrite via trackpad, souris ou ecran tactile.

Une demande ne peut pas etre scannee tant que la photo et la signature ne sont pas enregistrees.

## Workflows concernes

La fonctionnalite concerne les trois parcours :

- Nouveau titre ;
- Duplicata ;
- Transfert de visa.

## Nouveau workflow global

### Nouveau titre

1. Dossier cree
2. Signature terminee
3. Dossier scanne
4. Dossier termine

### Duplicata

1. Duplicata demande
2. Duplicata signature terminee
3. Duplicata scanne
4. Duplicata valide ou duplicata rejete
5. Duplicata emis si valide

### Transfert

1. Transfert demande
2. Transfert signature terminee
3. Transfert scanne
4. Transfert valide ou transfert rejete
5. Transfert emis si valide

## Organisation proposee des statuts

Pour garder une logique d'ID lisible, on insere le statut de signature avant le statut scanne.

| ID | Code | Libelle | Groupe |
| --- | --- | --- | --- |
| 1 | CREATION | Dossier cree | Nouveau titre |
| 2 | SIGNATURE_TERMINEE | Signature terminee | Nouveau titre |
| 3 | SCANNE | Dossier scanne | Nouveau titre |
| 4 | TERMINE | Dossier termine | Nouveau titre |
| 10 | DUPLICATA_DEMANDE | Duplicata demande | Duplicata |
| 11 | DUPLICATA_SIGNATURE_TERMINEE | Duplicata signature terminee | Duplicata |
| 12 | DUPLICATA_SCANNE | Duplicata scanne | Duplicata |
| 13 | DUPLICATA_VALIDE | Duplicata valide | Duplicata |
| 14 | DUPLICATA_REJETE | Duplicata rejete | Duplicata |
| 15 | DUPLICATA_EMIS | Duplicata emis | Duplicata |
| 20 | TRANSFERT_DEMANDE | Transfert demande | Transfert |
| 21 | TRANSFERT_SIGNATURE_TERMINEE | Transfert signature terminee | Transfert |
| 22 | TRANSFERT_SCANNE | Transfert scanne | Transfert |
| 23 | TRANSFERT_VALIDE | Transfert valide | Transfert |
| 24 | TRANSFERT_REJETE | Transfert rejete | Transfert |
| 25 | TRANSFERT_EMIS | Transfert emis | Transfert |

Si la base contient deja des donnees, il faudra prevoir une migration des anciennes valeurs :

- ancien `2` vers nouveau `3` pour `Dossier scanne` ;
- ancien `3` vers nouveau `4` pour `Dossier termine` ;
- ancien `11` vers nouveau `12` pour `Duplicata scanne` ;
- ancien `12` vers nouveau `13` pour `Duplicata valide` ;
- ancien `13` vers nouveau `14` pour `Duplicata rejete` ;
- ancien `14` vers nouveau `15` pour `Duplicata emis` ;
- ancien `21` vers nouveau `22` pour `Transfert scanne` ;
- ancien `22` vers nouveau `23` pour `Transfert valide` ;
- ancien `23` vers nouveau `24` pour `Transfert rejete` ;
- ancien `24` vers nouveau `25` pour `Transfert emis`.

## Table de base de donnees a ajouter

Nom propose : `Capture_signature`

```sql
CREATE TABLE Capture_signature (
    id_capture_signature BIGSERIAL PRIMARY KEY,
    id_demande BIGINT NOT NULL UNIQUE,
    photo_path VARCHAR(255) NOT NULL,
    signature_path VARCHAR(255) NOT NULL,
    date_capture TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_capture_signature_demande
        FOREIGN KEY (id_demande)
        REFERENCES Demande(id_demande)
);
```

Les fichiers peuvent etre stockes dans :

- `uploads/photos/` pour la photo webcam ;
- `uploads/signatures/` pour la signature.

La base conserve seulement les chemins des fichiers, comme pour les pieces justificatives.

## Backend

### Entite a ajouter

`CaptureSignature`

Champs :

- `id`
- `demande`
- `photoPath`
- `signaturePath`
- `dateCapture`

### Repository

`CaptureSignatureRepository`

Methodes utiles :

- `Optional<CaptureSignature> findByDemandeId(Long demandeId)`
- `boolean existsByDemandeId(Long demandeId)`

### Service

Ajouter un service dedie : `SignatureService`.

Responsabilites :

- verifier que la demande existe ;
- verifier que le statut courant autorise la capture ;
- enregistrer la photo webcam ;
- enregistrer la signature ;
- creer ou remplacer la ligne `Capture_signature` ;
- passer la demande au statut `Signature terminee`.

### Transitions autorisees

| Statut courant | Action | Nouveau statut |
| --- | --- | --- |
| Dossier cree | Valider photo + signature | Signature terminee |
| Duplicata demande | Valider photo + signature | Duplicata signature terminee |
| Transfert demande | Valider photo + signature | Transfert signature terminee |

### Scan

La methode de scan ne doit plus accepter les statuts de creation directe.

Nouvelles transitions de scan :

| Statut courant | Action | Nouveau statut |
| --- | --- | --- |
| Signature terminee | Scanner | Dossier scanne |
| Duplicata signature terminee | Scanner | Duplicata scanne |
| Transfert signature terminee | Scanner | Transfert scanne |

Avant le scan, le backend doit verifier :

- que la capture photo/signature existe ;
- que toutes les pieces justificatives necessaires sont importees.

## API proposee

### Recuperer la capture d'une demande

```http
GET /api/demandes/{id}/signature
```

Retour :

```json
{
  "id": 1,
  "demandeId": 42,
  "photoPath": "photos/demande-42-photo.png",
  "signaturePath": "signatures/demande-42-signature.png",
  "dateCapture": "2026-05-20T10:30:00"
}
```

### Enregistrer photo et signature

```http
POST /api/demandes/{id}/signature
Content-Type: multipart/form-data
```

Champs :

- `photo` : image capturee via webcam ;
- `signature` : image PNG generee depuis le canvas de signature.

Effet :

- sauvegarde les deux fichiers ;
- cree ou met a jour `Capture_signature` ;
- change le statut vers le statut `Signature terminee` correspondant au workflow.

## Frontend

### Page a ajouter

`src/main/resources/static/signature.html`

Fonctionnalites attendues :

- afficher les informations de la demande ;
- activer la webcam avec `navigator.mediaDevices.getUserMedia`;
- capturer une photo depuis la video vers un canvas ;
- afficher un apercu de la photo ;
- proposer une zone de signature canvas ;
- permettre d'effacer et refaire la signature ;
- desactiver le bouton de validation tant que la photo ou la signature manque ;
- envoyer photo + signature au backend ;
- rediriger vers `upload.html?id={id}` apres succes.

### Navigation

Apres creation :

- nouveau titre : rediriger vers `signature.html?id={id}` ;
- duplicata : rediriger vers `signature.html?id={id}` ;
- transfert : rediriger vers `signature.html?id={id}`.

Depuis la liste/detail :

- statut `Dossier cree` : bouton `Signer` ;
- statut `Duplicata demande` : bouton `Signer` ;
- statut `Transfert demande` : bouton `Signer` ;
- statut `Signature terminee` : bouton `Importer pieces / Scanner`.

## Regles metier

- Une demande ne peut pas passer au scan sans signature terminee.
- La photo et la signature sont obligatoires.
- Si une capture existe deja, l'agent peut la remplacer tant que le dossier n'est pas scanne.
- Apres scan, la capture devient consultable mais non modifiable.
- Chaque demande ne peut avoir qu'une seule capture active.

## Points d'implementation

1. Ajouter les nouveaux statuts dans `sql/create.sql` et `src/main/resources/data.sql`.
2. Mettre a jour les constantes dans `Demande`.
3. Mettre a jour les libelles de statut.
4. Ajouter la table `Capture_signature`.
5. Ajouter `CaptureSignature`, son repository, son service et son controller.
6. Modifier la logique de scan pour exiger le statut `Signature terminee`.
7. Modifier les pages de creation pour rediriger vers `signature.html`.
8. Modifier `demandes.html` et `detail.html` pour afficher les bons boutons selon le statut.
9. Ajouter les verifications backend avant validation de la signature et avant scan.

