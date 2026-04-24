# Documentation des Fonctionnalités : Duplicata et Transfert

Ce document décrit le fonctionnement technique et utilisateur des deux nouvelles fonctionnalités majeures du Back Office Visa.

---

## 1. Fonctionnalité : Duplicata de Carte de Résident

Le duplicata permet de réémettre une carte de résident existante en cas de perte, vol ou détérioration.

### Processus Utilisateur
1. **Recherche** : L'utilisateur recherche le dossier original via l'ID de demande ou la référence de la carte.
2. **Saisie des détails** :
   - Sélection du motif (Perte, Vol, Détérioration, Autre).
   - Saisie de la date de déclaration (obligatoire pour Perte/Vol).
   - Saisie de la date de délivrance du duplicata.
3. **Validation** : La demande est créée avec un statut spécifique (ID 10 : Duplicata demandé).

### Logique Backend
- **Service** : `DuplicataService.java`
- **API** : `/api/cartes-resident/duplicata`
- **Statuts associés** :
  - `10` : Duplicata demandé
  - `11` : Duplicata validé
  - `12` : Duplicata rejeté
  - `13` : Duplicata émis

---

## 2. Fonctionnalité : Transfert de Visa

Le transfert est utilisé lorsqu'un résident change de passeport (expiration, perte, etc.) et doit transférer son visa valide sur le nouveau document.

### Processus Utilisateur
1. **Recherche** : Identification du dossier via l'ancien numéro de passeport ou la référence de la carte.
2. **Saisie du Nouveau Passeport** :
   - Nouveau numéro de passeport.
   - Pays de délivrance.
   - Dates de délivrance et d'expiration.
3. **Soumission** : Création d'une demande de transfert (Statut ID 20).

### Logique Backend
- **Service** : `TransfertService.java`
- **API** : `/api/cartes-resident/transfert`
- **Entité dédiée** : `DemandeTransfert` (stocke les informations du nouveau passeport).
- **Statuts associés** :
  - `20` : Transfert demandé
  - `21` : Transfert validé
  - `22` : Transfert rejeté
  - `23` : Transfert émis

---

## 3. Interface et Navigation

### Fichiers Frontend
- `duplicata.html` : Interface dédiée à la création de duplicatas.
- `transfert.html` : Interface dédiée à la création de transferts.
- `demandes.html` : Liste globale permettant de filtrer et suivre ces demandes.
- `detail.html` : Vue détaillée permettant de valider ou rejeter les demandes de duplicata/transfert.

### Thème Visuel
- Les deux pages utilisent le thème **Bleu Madagascar** (`#1a5276`) pour une cohérence totale avec le reste du Back Office.
- Les badges de statut sont colorés différemment pour une identification rapide dans la liste des demandes.
