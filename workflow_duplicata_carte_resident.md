# Workflow — Duplicata de Carte de Résident

## 1) Constat sur l’existant (dans ce projet)

### Quand obtient-on une carte de résident aujourd’hui ?

Dans le code actuel:

- Il existe une entité JPA `CarteResident` + `CarteResidentRepository`.
- Mais il n’y a **aucun endpoint** et aucune logique métier qui **crée** une `CarteResident`.
- Le workflow implémenté aujourd’hui s’arrête à:
  - création d’un **dossier de demande** (`Demande`) avec statut:
    - `1` = *Dossier créé*
  - puis action *scanner*:
    - `2` = *Dossier scanné*

Conclusion: **dans l’état actuel, on n’obtient jamais une carte de résident** via l’application. Il manque un étape “décision/validation” + “émission carte” (et probablement l’émission d’un `Visa` aussi).

> Le fichier `scenario_demande_visa.txt` décrit des statuts plus complets (brouillon/soumise/en cours/validée/rejetée…), mais ils ne sont pas implémentés dans le code actuel.

## 2) Compréhension de la nouvelle fonctionnalité demandée

Tu veux ajouter une fonctionnalité **Duplicata Carte de Résident** avec deux chemins:

- **Chemin A (dossier déjà terminé)**
  - Le demandeur existe déjà dans le système via une `Demande` “terminée”.
  - On peut déposer une **demande de duplicata**.
  - Si elle est **validée**, on “fait le duplicata” (i.e. on émet une nouvelle carte, ou un nouvel enregistrement).

- **Chemin B (pas encore de données demandeur / pas de dossier existant)**
  - On n’a pas encore les **informations personnelles** du demandeur dans la base.
  - On doit saisir les infos **comme une demande classique** (identité, passeport, etc.).
  - La différence: le dossier ne passe pas par “créé” seulement, il doit aller vers “terminé”, puis le duplicata continue comme le chemin A.

## 3) Proposition de workflow cible (statuts + étapes)

### 3.1 Statuts recommandés (cible)

Pour supporter correctement le duplicata, il est utile d’avoir des statuts plus riches (au minimum):

- `CREATION` (brouillon / dossier créé)
- `SCANNE` (dossier scanné / verrouillé)
- `TERMINE` (dossier traité/clos, décision prise)

Et pour le duplicata (nouvelle demande duplicata):

- `DUPLICATA_DEMANDE` (demande déposée)
- `DUPLICATA_VALIDE` (acceptée)
- `DUPLICATA_REJETE` (refusée)
- `DUPLICATA_EMIS` (duplicata généré)

> Remarque: tu peux soit implémenter ces statuts dans `Demande.statut`, soit créer une entité dédiée `DemandeDuplicata` avec ses statuts propres. Le scénario ci-dessous marche dans les deux cas.

### 3.2 Règles d’éligibilité

Une demande de duplicata carte de résident est **éligible** si:

- Il existe une **carte de résident déjà émise** (ou au moins une demande `TERMINE` avec une carte liée).
- La demande d’origine est **terminée** (pas en cours).

Cas non éligibles:

- Dossier pas terminé.
- Demandeur introuvable.
- Carte résident introuvable (si on exige qu’elle existe déjà pour demander un duplicata).

## 4) Scénario complet — Duplicata Carte de Résident

### Acteurs

- **Agent** (back-office) : saisit/valide/scanne
- **Système** : applique les contrôles et change les statuts

## 4.1 Chemin A — Demandeur déjà connu (dossier déjà terminé)

### Étape A1 — Recherche du demandeur / dossier terminé

- L’agent ouvre “Duplicata carte de résident”.
- Il recherche par:
  - numéro passeport, ou
  - référence carte (si existante), ou
  - référence visa transformable / demandeId.

**Résultat attendu**:

- Le système retrouve:
  - une `Demande` avec statut `TERMINE` (ou équivalent)
  - et idéalement une `CarteResident` associée.

### Étape A2 — Vérification d’éligibilité

Le système vérifie:

- dossier `TERMINE`.
- carte résident existe.

Si KO:

- afficher la raison (ex: “dossier pas terminé”, “carte introuvable”).

### Étape A3 — Création de la demande de duplicata

L’agent renseigne:

- motif duplicata (perte/vol/détérioration/autre)
- date de déclaration (si nécessaire)
- référence de l’ancienne carte (si connue)
- pièces justificatives duplicata (ex: déclaration de perte, etc.)

Le système crée une **demande duplicata** avec statut:

- `DUPLICATA_DEMANDE`

### Étape A4 — Instruction / validation

L’agent (ou un superviseur) décide:

- **Valider** → statut `DUPLICATA_VALIDE`
- **Rejeter** → statut `DUPLICATA_REJETE` + motif

### Étape A5 — Émission du duplicata

Si validée:

- le système “émet” une nouvelle carte (duplicata):
  - soit en créant un **nouvel enregistrement** `CarteResident` lié à la demande duplicata
  - soit en créant une nouvelle `CarteResident` liée à la `Demande` d’origine avec une nouvelle référence (mais ça rend l’historique plus difficile)

Statut final:

- `DUPLICATA_EMIS`

Sorties:

- nouvelle référence carte résident
- dates (début/fin) selon règle (souvent: mêmes dates que l’ancienne carte, ou recalcul si renouvellement)

## 4.2 Chemin B — Pas de données demandeur (on crée un dossier “directement terminé”)

### Intention métier

Tu veux pouvoir faire un duplicata même si le système n’a pas encore le demandeur. Donc on doit:

1) saisir les informations (comme une demande)
2) marquer ce dossier comme **TERMINÉ** (au lieu de passer par le workflow normal)
3) enchaîner sur la demande duplicata

### Étape B1 — Saisie “identité + passeport + contexte”

L’agent renseigne (comme `index.html` aujourd’hui):

- Informations personnelles
- Passeport
- Référentiels (nationalité, situation)

Puis, spécifiquement pour “données manquantes”:

- on demande la **référence / preuve** de l’ancienne carte (si dispo)
- ou une preuve alternative (selon règles)

### Étape B2 — Création d’un dossier “terminé”

Le système crée:

- `Demandeur`
- `Passeport`
- une `Demande` (ou un objet “dossier”) avec **statut = `TERMINE`**

But: permettre au duplicata de s’appuyer sur un dossier complet.

> C’est un raccourci: tu assumes que les contrôles habituels (scan, traitement) ont été faits hors-système.

### Étape B3 — Création de la carte résident “source” (si nécessaire)

Si ta règle d’éligibilité exige qu’une carte existe déjà:

- créer une `CarteResident` (ancienne carte) avec référence fournie

Sinon, tu peux passer directement à B4.

### Étape B4 — Enchaîner sur A3 (demande duplicata)

À partir de là, on reprend le **workflow duplicata standard**:

- création de la demande duplicata `DUPLICATA_DEMANDE`
- validation/rejet
- émission

## 5) Écrans / pages (suggestion)

- Page “Duplicata carte de résident”
  - onglet 1: Recherche (Chemin A)
  - onglet 2: Création dossier manquant (Chemin B)
  - onglet 3: Liste des demandes duplicata + statut

## 6) APIs (suggestion de contrats)

### Référentiels duplicata

- `GET /api/references/motifs-duplicata` (optionnel)
- `GET /api/references/pieces-duplicata` (optionnel)

### Duplicata

- `POST /api/cartes-resident/duplicata/recherche` (ou `GET` par query)
- `POST /api/cartes-resident/duplicata` (création demande duplicata)
- `PUT /api/cartes-resident/duplicata/{id}/valider`
- `PUT /api/cartes-resident/duplicata/{id}/rejeter`
- `PUT /api/cartes-resident/duplicata/{id}/emettre`

### Création dossier “terminé” (Chemin B)

- `POST /api/dossiers/termines` (crée demandeur + passeport + demande en statut `TERMINE`)

## 7) Points d’attention (importants)

- Unicité:
  - `Passeport.numeroPasseport` est unique dans la DB → si un duplicata arrive avec un passeport déjà existant, il faut décider si tu:
    - réutilises le demandeur existant, ou
    - refuses, ou
    - merges.
- Historique:
  - le duplicata doit garder un lien vers:
    - la carte d’origine, ou
    - la demande d’origine.
- Dates carte:
  - définir clairement comment sont calculées `dateDebut/dateFin` du duplicata.

