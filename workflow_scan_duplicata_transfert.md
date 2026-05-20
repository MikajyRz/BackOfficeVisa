# Workflow scan - Duplicata et transfert de visa

## 1. Objectif

Aujourd'hui, les demandes de duplicata et de transfert passent directement de:

- `DUPLICATA_DEMANDE` vers `DUPLICATA_VALIDE` ou `DUPLICATA_REJETE`
- `TRANSFERT_DEMANDE` vers `TRANSFERT_VALIDE` ou `TRANSFERT_REJETE`

Le nouveau fonctionnement demande que ces deux workflows suivent la meme logique que le **Nouveau titre**:

1. creation de la demande
2. scan du dossier physique
3. decision: validation ou rejet
4. emission si la demande est validee

Le scan devient donc une etape obligatoire avant toute validation ou tout rejet.

## 2. Workflow actuel de reference: Nouveau titre

Statuts existants:

| Code | Statut | Sens metier |
| --- | --- | --- |
| `1` | `STATUT_CREATION` | Dossier cree |
| `2` | `STATUT_SCANNE` | Dossier scanne |
| `3` | `STATUT_TERMINE` | Dossier termine |

Transition actuelle:

```text
Dossier cree (1)
  -> scanner
Dossier scanne (2)
  -> terminer
Dossier termine (3)
```

Regle importante:

- un dossier ne peut pas etre termine tant qu'il n'est pas scanne.

## 3. Nouveau workflow cible: Duplicata

### 3.1 Statuts proposes

Les statuts existants sont conserves, et un nouveau statut est ajoute pour le scan.

| Code | Statut | Sens metier |
| --- | --- | --- |
| `10` | `STATUT_DUPLICATA_DEMANDE` | Demande de duplicata creee |
| `11` | `STATUT_DUPLICATA_SCANNE` | Dossier duplicata scanne |
| `12` | `STATUT_DUPLICATA_VALIDE` | Demande de duplicata validee |
| `13` | `STATUT_DUPLICATA_REJETE` | Demande de duplicata rejetee |
| `14` | `STATUT_DUPLICATA_EMIS` | Duplicata emis |

> Note: le code `11` est utilise pour eviter de renumeroter les statuts existants `11`, `12`, `13`.

### 3.2 Transitions autorisees

```text
Duplicata demande (10)
  -> scanner
Duplicata scanne (11)
  -> valider
Duplicata valide (12)
  -> emettre
Duplicata emis (14)
```

Chemin de rejet:

```text
Duplicata demande (10)
  -> scanner
Duplicata scanne (11)
  -> rejeter
Duplicata rejete (13)
```

### 3.3 Regles metier

- La creation d'une demande de duplicata garde le statut initial `10`.
- L'action `scanner` est obligatoire avant `valider` ou `rejeter`.
- L'action `valider` est autorisee uniquement si le statut courant est `14`.
- L'action `rejeter` est autorisee uniquement si le statut courant est `14`.
- L'action `emettre` est autorisee uniquement si le statut courant est `11`.
- Chaque changement de statut doit etre ajoute dans `StatutDemande`.
- Apres scan, le dossier doit etre considere comme verrouille, comme pour un nouveau titre.

## 4. Nouveau workflow cible: Transfert de visa

### 4.1 Statuts proposes

Les statuts existants sont conserves, et un nouveau statut est ajoute pour le scan.

| Code | Statut | Sens metier |
| --- | --- | --- |
| `20` | `STATUT_TRANSFERT_DEMANDE` | Demande de transfert creee |
| `21` | `STATUT_TRANSFERT_SCANNE` | Dossier transfert scanne |
| `22` | `STATUT_TRANSFERT_VALIDE` | Demande de transfert validee |
| `23` | `STATUT_TRANSFERT_REJETE` | Demande de transfert rejetee |
| `24` | `STATUT_TRANSFERT_EMIS` | Transfert emis |

> Note: le code `21` est utilise pour eviter de renumeroter les statuts existants `21`, `22`, `23`.

### 4.2 Transitions autorisees

```text
Transfert demande (20)
  -> scanner
Transfert scanne (21)
  -> valider
Transfert valide (22)
  -> emettre
Transfert emis (24)
```

Chemin de rejet:

```text
Transfert demande (20)
  -> scanner
Transfert scanne (21)
  -> rejeter
Transfert rejete (23)
```

### 4.3 Regles metier

- La creation d'une demande de transfert garde le statut initial `20`.
- L'action `scanner` est obligatoire avant `valider` ou `rejeter`.
- L'action `valider` est autorisee uniquement si le statut courant est `24`.
- L'action `rejeter` est autorisee uniquement si le statut courant est `24`.
- L'action `emettre` est autorisee uniquement si le statut courant est `21`.
- Chaque changement de statut doit etre ajoute dans `StatutDemande`.
- Apres scan, le dossier doit etre considere comme verrouille, comme pour un nouveau titre.

## 5. APIs cibles

### 5.1 Nouveau titre

Deja existant:

- `PUT /api/demandes/{id}/scanner`

### 5.2 Duplicata

Endpoints existants a completer:

- `POST /api/cartes-resident/duplicata`
- `PUT /api/cartes-resident/duplicata/{id}/valider`
- `PUT /api/cartes-resident/duplicata/{id}/rejeter`
- `PUT /api/cartes-resident/duplicata/{id}/emettre`

Nouvel endpoint propose:

- `PUT /api/cartes-resident/duplicata/{id}/scanner`

Comportement:

- verifie que la demande existe
- verifie que son statut est `STATUT_DUPLICATA_DEMANDE`
- verifie les pieces/fichiers requis si le workflow duplicata gere des pieces scannees
- passe le statut a `STATUT_DUPLICATA_SCANNE`
- enregistre l'historique dans `StatutDemande`

### 5.3 Transfert de visa

Endpoints existants a completer:

- `POST /api/cartes-resident/transfert`
- `PUT /api/cartes-resident/transfert/{id}/valider`
- `PUT /api/cartes-resident/transfert/{id}/rejeter`
- `PUT /api/cartes-resident/transfert/{id}/emettre`

Nouvel endpoint propose:

- `PUT /api/cartes-resident/transfert/{id}/scanner`

Comportement:

- verifie que la demande existe
- verifie que son statut est `STATUT_TRANSFERT_DEMANDE`
- verifie les pieces/fichiers requis si le workflow transfert gere des pieces scannees
- passe le statut a `STATUT_TRANSFERT_SCANNE`
- enregistre l'historique dans `StatutDemande`

## 6. Changements backend a prevoir

### 6.1 `Demande.java`

Ajouter les constantes:

```java
public static final int STATUT_DUPLICATA_SCANNE = 11;
public static final int STATUT_TRANSFERT_SCANNE = 21;
```

Ajouter les libelles dans `getStatutLibelle()`:

```java
case STATUT_DUPLICATA_SCANNE -> "Duplicata scanne";
case STATUT_TRANSFERT_SCANNE -> "Transfert scanne";
```

### 6.2 `DuplicataService.java`

Ajouter une methode:

```java
@Transactional
public void scannerDuplicata(Long duplicataDemandeId) {
    Demande demande = demandeRepository.findById(duplicataDemandeId)
            .orElseThrow(() -> new RuntimeException("Demande de duplicata introuvable"));

    if (demande.getStatut() != Demande.STATUT_DUPLICATA_DEMANDE) {
        throw new RuntimeException("Seule une demande de duplicata au statut 'demande' peut etre scannee");
    }

    demande.setStatut(Demande.STATUT_DUPLICATA_SCANNE);
    demandeRepository.save(demande);
    enregistrerChangementStatut(demande, Demande.STATUT_DUPLICATA_SCANNE);
}
```

Modifier les controles:

- `validerDuplicata` doit accepter uniquement `STATUT_DUPLICATA_SCANNE`.
- `rejeterDuplicata` doit accepter uniquement `STATUT_DUPLICATA_SCANNE`.
- `emettreDuplicata` reste limite a `STATUT_DUPLICATA_VALIDE`.

### 6.3 `DuplicataController.java`

Ajouter:

```java
@PutMapping("/{id}/scanner")
public ResponseEntity<?> scanner(@PathVariable("id") Long id) {
    duplicataService.scannerDuplicata(id);
    return ResponseEntity.ok(Map.of("message", "Demande de duplicata scannee"));
}
```

### 6.4 `TransfertService.java`

Ajouter une methode:

```java
@Transactional
public void scannerTransfert(Long transfertDemandeId) {
    Demande demande = demandeRepository.findById(transfertDemandeId)
            .orElseThrow(() -> new RuntimeException("Demande de transfert introuvable"));

    if (demande.getStatut() != Demande.STATUT_TRANSFERT_DEMANDE) {
        throw new RuntimeException("Seule une demande de transfert au statut 'demande' peut etre scannee");
    }

    demande.setStatut(Demande.STATUT_TRANSFERT_SCANNE);
    demandeRepository.save(demande);
    enregistrerChangementStatut(demande, Demande.STATUT_TRANSFERT_SCANNE);
}
```

Modifier les controles:

- `validerTransfert` doit accepter uniquement `STATUT_TRANSFERT_SCANNE`.
- `rejeterTransfert` doit accepter uniquement `STATUT_TRANSFERT_SCANNE`.
- `emettreTransfert` doit accepter uniquement `STATUT_TRANSFERT_VALIDE`.

### 6.5 `TransfertController.java`

Ajouter:

```java
@PutMapping("/{id}/scanner")
public ResponseEntity<?> scanner(@PathVariable("id") Long id) {
    transfertService.scannerTransfert(id);
    return ResponseEntity.ok(Map.of("message", "Demande de transfert scannee"));
}
```

## 7. Changements frontend a prevoir

### 7.1 Badges de statut

Ajouter les classes:

- `badge-duplicata-scanne`
- `badge-transfert-scanne`

Ajouter les correspondances:

- `14` -> `badge-duplicata-scanne`
- `24` -> `badge-transfert-scanne`

### 7.2 Detail d'une demande

Afficher les actions selon le statut:

| Statut courant | Actions visibles |
| --- | --- |
| `10` Duplicata demande | Scanner |
| `14` Duplicata scanne | Valider, Rejeter |
| `11` Duplicata valide | Emettre |
| `20` Transfert demande | Scanner |
| `24` Transfert scanne | Valider, Rejeter |
| `21` Transfert valide | Emettre |

Les boutons `Valider` et `Rejeter` ne doivent plus etre visibles pour les statuts `10` et `20`.

## 8. Criteres d'acceptation

- Une demande de duplicata creee obtient le statut `10`.
- Une demande de duplicata ne peut pas etre validee ou rejetee au statut `10`.
- Une demande de duplicata doit passer par `scanner`, puis obtient le statut `14`.
- Une demande de duplicata scannee peut etre validee ou rejetee.
- Une demande de duplicata validee peut etre emise.
- Une demande de transfert creee obtient le statut `20`.
- Une demande de transfert ne peut pas etre validee ou rejetee au statut `20`.
- Une demande de transfert doit passer par `scanner`, puis obtient le statut `24`.
- Une demande de transfert scannee peut etre validee ou rejetee.
- Une demande de transfert validee peut etre emise.
- L'historique affiche toutes les transitions.
- Les badges et boutons frontend correspondent au statut courant.

## 9. Tests manuels recommandes

### 9.1 Duplicata

1. Creer une demande de duplicata.
2. Verifier que le statut est `Duplicata demande`.
3. Tenter de valider directement: l'action doit etre refusee.
4. Scanner la demande.
5. Verifier que le statut est `Duplicata scanne`.
6. Valider la demande.
7. Verifier que le statut est `Duplicata valide`.
8. Emettre le duplicata.
9. Verifier que le statut est `Duplicata emis`.

### 9.2 Transfert

1. Creer une demande de transfert.
2. Verifier que le statut est `Transfert demande`.
3. Tenter de valider directement: l'action doit etre refusee.
4. Scanner la demande.
5. Verifier que le statut est `Transfert scanne`.
6. Valider la demande.
7. Verifier que le statut est `Transfert valide`.
8. Emettre le transfert.
9. Verifier que le statut est `Transfert emis`.


