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

Tu veux ajouter une fonctionnalité **Duplicata Carte de Résident** avec deux cas:

- **Cas 1 (avec données antérieures)**
  - Le demandeur existe déjà dans le système (état civil, passeport, etc.).
  - Le dossier est **accepté/terminé**.
  - Il existe déjà une **Carte de résident**.
  - On remplit le **formulaire de duplicata**, puis on enregistre/émet le duplicata.

- **Cas 2 (sans données antérieures)**
  - Le demandeur n’existe pas encore en base (pas d’état civil/passeport/enregistrements).
  - On crée **2 demandes**:
    - Demande 1: **Transformation** (formulaire sprint 1) pour créer le demandeur + passeport + infos nécessaires, puis statut: **demande acceptée**.
    - Demande 2: **Duplicata carte de résident** (rediriger vers formulaire de duplicata) puis enregistrer/émettre le duplicata.

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

## 4.1 Cas 1 — Avec données antérieures

### Étape 1 — Recherche du demandeur / dossier accepté

- L’agent ouvre “Duplicata carte de résident”.
- Il recherche par:
  - numéro passeport, ou
  - référence carte (si existante), ou
  - référence visa transformable / demandeId.

**Résultat attendu**:

- Le système retrouve:
  - un demandeur existant (état civil + passeport)
  - une `Demande` d’origine **acceptée/terminée**
  - une `CarteResident` associée

### Étape 2 — Vérification d’éligibilité

Le système vérifie:

- dossier **accepté/terminé**.
- carte résident existe.

Si KO:

- afficher la raison (ex: “dossier pas terminé”, “carte introuvable”).

### Étape 3 — Formulaire de duplicata (saisie)

L’agent renseigne:

- motif de perte (perte/vol/détérioration/autre)
- date de déclaration (si nécessaire)
- référence de l’ancienne carte (si connue)
- nouvelles dates:
  - date de délivrance (duplicata)
  - date d’expiration (duplicata)
- pièces justificatives duplicata (ex: déclaration de perte, etc.)

Le système crée une **demande duplicata** avec statut:

- `DUPLICATA_DEMANDE`

### Étape 4 — Instruction / validation

L’agent (ou un superviseur) décide:

- **Valider** → statut `DUPLICATA_VALIDE`
- **Rejeter** → statut `DUPLICATA_REJETE` + motif

### Étape 5 — Émission / enregistrement du duplicata

Si validée:

- le système “émet” une nouvelle carte (duplicata):
  - soit en créant un **nouvel enregistrement** `CarteResident` lié à la demande duplicata
  - soit en créant une nouvelle `CarteResident` liée à la `Demande` d’origine avec une nouvelle référence (mais ça rend l’historique plus difficile)

Statut final:

- `DUPLICATA_EMIS`

Sorties:

- nouvelle référence carte résident
- dates (délivrance/expiration) selon règle (cf. section 4.3)

## 4.2 Cas 2 — Sans données antérieures (2 demandes)

### Intention métier

Tu veux pouvoir faire un duplicata même si le système n’a pas encore le demandeur. Donc on doit:

1) créer une **Demande 1: Transformation** (sprint 1) qui enregistre état civil + passeport, puis statut: **acceptée**
2) rediriger vers **Demande 2: Duplicata**
3) enregistrer/émettre le duplicata

### Étape 1 — Demande 1: Transformation (création des données)

L’agent renseigne (formulaire transformation sprint 1):

- Informations personnelles
- Passeport
- Référentiels (nationalité, situation)

Puis, spécifiquement pour ce cas:

- on demande la **référence / preuve** de l’ancienne carte (si dispo)
- ou une preuve alternative (selon règles)

### Étape 2 — Validation: Demande transformation acceptée

Le système crée:

- `Demandeur`
- `Passeport`
- une `Demande` de transformation avec statut **acceptée/terminée**

But: permettre au duplicata de s’appuyer sur un dossier complet.

> C’est un raccourci: tu assumes que les contrôles habituels (scan, traitement) ont été faits hors-système.

### Étape 3 — Existence de la carte résident (source)

Si ta règle d’éligibilité exige qu’une carte existe déjà:

- créer une `CarteResident` (ancienne carte) avec référence fournie

Sinon, tu peux passer directement à B4.

### Étape 4 — Demande 2: Duplicata

À partir de là, on reprend le **workflow duplicata standard** (section 4.1):

- formulaire duplicata
- création de la demande duplicata `DUPLICATA_DEMANDE`
- validation/rejet
- émission

## 4.3 Règles proposées pour les dates de délivrance/expiration du duplicata

Le formulaire duplicata demande:

- `dateDelivranceDuplicata`
- `dateExpirationDuplicata`

Proposition de logique (simple et contrôlable):

1) **Date de délivrance**
   - par défaut: `aujourd’hui`
   - modifiable manuellement par l’agent (si besoin)

2) **Date d’expiration** (2 variantes possibles)
   - Variante V1 (recommandée pour un duplicata strict): garder la même `dateFin` que la carte d’origine
   - Variante V2 (si vous voulez “réémettre”): recalculer une nouvelle période à partir de la date de délivrance

3) **Validations**
   - `dateExpirationDuplicata` doit être strictement > `dateDelivranceDuplicata`
   - `dateExpirationDuplicata` ne doit pas dépasser une limite administrative (ex: +10 ans) si vous fixez une règle
   - si V1: `dateExpirationDuplicata` est forcée à la date d’expiration de la carte d’origine

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

