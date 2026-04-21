# BackOfficeVisa

Application **Back Office Visa — Madagascar**.

- Backend: **Java 17**, **Spring Boot** (Spring WebMVC, Spring Data JPA)
- Base de données: **PostgreSQL**
- Frontend: pages **HTML/CSS/JS** servies en statique par Spring Boot (`src/main/resources/static`)

## Démarrage rapide

### Prérequis

- Java 17
- Maven (ou utiliser le wrapper `mvnw` / `mvnw.cmd`)
- PostgreSQL

### Base de données

La configuration par défaut est dans `src/main/resources/application.properties`:

- `spring.datasource.url=jdbc:postgresql://localhost:5432/visa`
- `spring.datasource.username=postgres`
- `spring.datasource.password=postgres`

Le projet utilise:

- `spring.jpa.hibernate.ddl-auto=update`
- `spring.sql.init.mode=always`
- `spring.jpa.defer-datasource-initialization=true`

Un script d’initialisation des données de référence est présent dans:

- `src/main/resources/data.sql`

Un script SQL complet (recréation tables + insertion références) est présent dans:

- `sql/create.sql`

### Lancer l’application

Depuis la racine du projet:

```bash
./mvnw spring-boot:run
```

Sous Windows:

```powershell
.\mvnw.cmd spring-boot:run
```

L’application démarre via `com.backoffice.visa.VisaApplication`.

### Accès UI (front statique)

Les pages statiques sont dans `src/main/resources/static` et sont servies par Spring Boot.

- `http://localhost:8080/index.html` : création d’une nouvelle demande (formulaire multi-étapes)
- `http://localhost:8080/demandes.html` : liste des demandes + filtres + action “scanner”
- `http://localhost:8080/detail.html?id={id}` : détail d’une demande (infos + pièces + action “scanner”)

## Architecture (high level)

Le code est organisé sous `src/main/java/com/backoffice/visa`:

- `controller/`
  - `DemandeController` : endpoints liés aux demandes
  - `ReferenceController` : endpoints de données de référence (nationalités, types, pièces…)
- `service/`
  - `DemandeService` : logique métier de création + contrôle + scan
- `repository/`
  - Repositories JPA (accès DB)
- `entity/`
  - Entités JPA (modèle de données)
- `dto/`
  - `DemandeFormDTO` : payload JSON attendu pour créer une demande

## Modèle de données (principales entités)

- `Demandeur` : informations personnelles (nom, prénom, naissance, contact, adresse) + nationalité + situation familiale
- `Passeport` : numéro (unique), dates, pays de délivrance (lié à un demandeur)
- `VisaTransformable` : numéro de référence (unique), lieu, dates (lié à demandeur + passeport)
- `Demande` : type visa + type demande + dates + statut + lien vers `VisaTransformable`
- `PieceDemande` : pièces communes cochées pour une demande
- `PieceDemandeSpecifique` : pièces spécifiques (selon type de visa) cochées pour une demande
- `StatutDemande` : historique des changements de statut
- Référentiels: `Nationalite`, `SituationFamiliale`, `TypeVisa`, `TypeDemande`, `TypePieceCommune`, `TypePieceSpecifique`

## Fonctionnalités existantes

### 1) Gestion des données de référence

Endpoints exposés par `ReferenceController` (`/api/references`):

- `GET /api/references/nationalites`
- `GET /api/references/situations-familiales`
- `GET /api/references/types-visa`
- `GET /api/references/types-demande`
- `GET /api/references/pieces-communes`
- `GET /api/references/pieces-specifiques/{typeVisaId}`

Ces données sont consommées par le frontend (remplissage des listes déroulantes + affichage des checkboxes pièces).

### 2) Création d’une demande (workflow)

Frontend: `index.html`.

- Étape 1: identité du demandeur
- Étape 2: informations passeport
- Étape 3: choix type de demande + type visa + saisie des informations de visa (référence/lieu/dates)
- Étape 4: pièces justificatives (communes + spécifiques)
- Étape 5: récapitulatif et enregistrement

Backend:

- `POST /api/demandes`
  - Crée un **dossier complet** (demandeur + passeport + visa transformable + demande + pièces)
  - Contrôles:
    - unicité `numeroPasseport`
    - unicité `numeroReferenceVisa`
    - cohérence des dates du visa (`dateDebutVisa <= dateFinVisa`)
    - **date de la demande** doit être comprise dans la période du visa transformable
    - toutes les pièces obligatoires (communes + spécifiques) doivent être cochées
  - Réponse: `{ id, statut, message }`

### 3) Consultation des demandes

Backend:

- `GET /api/demandes` : liste des demandes
- `GET /api/demandes/{id}` : détail d’une demande
- `GET /api/demandes/{id}/pieces` : pièces communes d’une demande
- `GET /api/demandes/{id}/pieces-specifiques` : pièces spécifiques d’une demande

Frontend:

- `demandes.html` : table + filtres (recherche nom/prénom, statut, type demande, type visa)
- `detail.html` : affichage complet + pièces + statut

### 4) Scan / verrouillage du dossier

Backend:

- `PUT /api/demandes/{id}/scanner`
  - Passage du statut:
    - `1` = **Dossier créé**
    - `2` = **Dossier scanné**
  - Effets:
    - set `dateTraitement=LocalDate.now()`
    - enregistre un historique dans `StatutDemande`
    - le service indique que le dossier “ne peut plus être modifié après scan” (logique à compléter si vous ajoutez des endpoints de modification)

### 5) Vérifications d’unicité (utilisées côté front)

- `GET /api/demandes/verifier-passeport/{numero}` → `{ existe: boolean }`
- `GET /api/demandes/verifier-numero-visa/{numero}` → `{ existe: boolean }`

### 6) Vérification d’une référence de visa déjà utilisée dans une demande

- `GET /api/demandes/visa-reference/{numero}`
  - si trouvé: `{ existe: true, lieu, dateDebut, dateFin }`
  - sinon: `{ existe: false }`

## Notes importantes / limitations actuelles

- Pas d’authentification/autorisation (aucune gestion de rôles trouvée dans le code).
- Pas d’API de modification/suppression de demande: seul le **scan** est implémenté en mutation après création.
- Le modèle de statuts est actuellement minimal dans le code (1/2). Un document `scenario_demande_visa.txt` décrit des statuts plus riches (brouillon/soumise/en cours/validée/rejetée) mais ils ne sont pas implémentés tels quels dans les entités/services actuels.

## Pistes d’extension (pour ajouter une nouvelle fonctionnalité)

- Ajouter un vrai **workflow de statuts** (ex: soumission/validation/rejet) + endpoints associés.
- Ajouter l’upload des pièces (au lieu de simples checkboxes) + stockage (DB, filesystem, S3...).
- Ajouter une couche **auth** (Spring Security) + rôles (agent, superviseur, admin).
- Ajouter des filtres côté backend (pagination, tri) au lieu de filtrer uniquement côté frontend.
