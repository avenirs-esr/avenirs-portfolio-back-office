# Règles d'import — Établissements, Groupes et Utilisateurs externes

Ce document décrit le corps (`body`) JSON attendu par les endpoints d'administration
d'import / mise à jour des trois entités structurantes du back-office :

- **`InstitutionData`** — établissement
- **`GroupData`** — groupe (formation, parcours, groupe d'étudiants)
- **`ExternalUserData`** — utilisateur externe

## 1. Généralités

| Point | Valeur |
|---|---|
| Authentification | En-tête `X-ADMIN-TOKEN: <admin.token>` obligatoire |
| Content-Type | `application/json` |
| Corps | **Toujours un tableau JSON** (`[ { ... }, { ... } ]`), même pour un seul élément |
| Format des dates | ISO `yyyy-MM-dd` (ex. `"2024-09-01"`) |
| Énumérations | Sérialisées par leur **nom exact en MAJUSCULES** (`PROGRAM`, `PRIMARY`, `STUDENT`…) |
| Champs optionnels | Peuvent être omis ou valorisés à `null` |
| Format d'erreur | `{ "code": "<CODE>", "message": "<message>" }` |

### Endpoints

| Ressource | POST (import / upsert) | PUT (mise à jour seule) |
|---|---|---|
| Établissement | `POST /back-office/admin/institutions` | `PUT /back-office/admin/institutions` |
| Groupe | `POST /back-office/admin/groups` | `PUT /back-office/admin/groups` |
| Utilisateur externe | `POST /back-office/external-users` | `PUT /back-office/external-users` |

### Sémantique POST vs PUT

**POST = upsert tolérant.** Chaque élément est créé, ou mis à jour s'il existe déjà (recherche
sur la clé métier : `hai`, `idSiSco`, `eppn`). Les éléments en erreur métier n'interrompent pas
le traitement : ils sont retournés dans `failed[]`. Réponse :

```json
{
  "createdCount": 2,
  "updatedCount": 1,
  "failedCount": 1,
  "created": [ ... ],
  "updated": [ ... ],
  "failed": [ { "hai": "0350009Z", "message": "Institution not found" } ]
}
```

La clé identifiant l'élément en échec est `hai` pour les établissements, `idSiSco` pour les
groupes et `eppn` pour les utilisateurs externes.

**PUT = mise à jour stricte.** L'élément doit déjà exister (sinon `404`). **Aucune tolérance aux
erreurs** : la première erreur fait échouer toute la requête. Réponse : la liste des objets mis
à jour.

> ⚠️ Il n'y a pas de Bean Validation (`@Valid`) sur ces corps de requête : un champ obligatoire
> omis ne produit pas un `400` explicite mais une erreur d'intégrité (`500`). Renseignez
> systématiquement tous les champs marqués « Oui ».

---

## 2. `InstitutionData` — Établissement

### Schéma

| Champ | Type JSON | Obligatoire | Description |
|---|---|---|---|
| `name` | string | **Oui** | Nom de l'établissement |
| `hai` | string | **Oui** | Identifiant HAI, **unique** — sert de clé d'upsert / mise à jour |
| `siret` | string \| null | Non | Numéro SIRET (14 chiffres) |
| `siren` | string \| null | Non | Numéro SIREN (9 chiffres) |
| `type` | enum | **Oui** | `PRIMARY` \| `SECONDARY` |
| `parentHai` | string \| null | Conditionnel | `hai` de l'établissement parent |

### Règles métier

- `type = PRIMARY` → `parentHai` **doit être `null` / absent**
  (sinon `INSTITUTION_PRIMARY_CANNOT_HAVE_PARENT`, `400`).
- `type = SECONDARY` → `parentHai` **est obligatoire**
  (sinon `INSTITUTION_SECONDARY_REQUIRES_PARENT`, `400`).
- Le parent référencé par `parentHai` doit exister (`INSTITUTION_NOT_FOUND`, `404`) et être de
  type `PRIMARY` (`INSTITUTION_PARENT_MUST_BE_PRIMARY`, `400`).
- Hiérarchie sur **2 niveaux uniquement** : un `SECONDARY` ne peut pas être parent.
- Ordre d'import : importer les `PRIMARY` **avant** les `SECONDARY` qui les référencent.

### Exemple

```json
[
  {
    "name": "Université de Rennes",
    "hai": "0350001A",
    "siret": "13000550100015",
    "siren": "130005501",
    "type": "PRIMARY",
    "parentHai": null
  },
  {
    "name": "Université de Rennes - IUT",
    "hai": "0350002B",
    "siret": "13000550100023",
    "siren": "130005501",
    "type": "SECONDARY",
    "parentHai": "0350001A"
  }
]
```

---

## 3. `GroupData` — Groupe

### Schéma

| Champ | Type JSON | Obligatoire | Description |
|---|---|---|---|
| `name` | string | **Oui** | Libellé du groupe |
| `idSiSco` | string | **Oui** | Identifiant SI Scolarité, **unique** — clé d'upsert / mise à jour |
| `institutionId` | string (UUID) | **Oui** | **UUID** de l'établissement de rattachement (pas le `hai`) |
| `codeSise` | string \| null | Non | Code SISE |
| `startDate` | string (`yyyy-MM-dd`) \| null | Non | Date de début |
| `endDate` | string (`yyyy-MM-dd`) \| null | Non | Date de fin |
| `type` | enum | **Oui** | `PROGRAM` \| `PROGRAM_OPTION` \| `STUDENT_GROUP` |
| `parentIdSiSco` | string \| null | Conditionnel | `idSiSco` du groupe parent |

> `institutionId` attend l'**UUID** renvoyé par `GET /back-office/admin/institutions`
> (champ `id`), et non le `hai`. À l'inverse, le parent est référencé par son `idSiSco`.

### Règles métier (hiérarchie par `type`)

| `type` | `parentIdSiSco` | Type du parent exigé |
|---|---|---|
| `PROGRAM` | **doit être `null`** | — |
| `PROGRAM_OPTION` | **obligatoire** | `PROGRAM` |
| `STUDENT_GROUP` | **obligatoire** | `PROGRAM` ou `PROGRAM_OPTION` |

Codes d'erreur associés (`400`) : `GROUP_PROGRAM_CANNOT_HAVE_PARENT`,
`GROUP_PROGRAM_OPTION_REQUIRES_PARENT`, `GROUP_PROGRAM_OPTION_PARENT_MUST_BE_PROGRAM`,
`GROUP_STUDENT_GROUP_REQUIRES_PARENT`, `GROUP_STUDENT_GROUP_PARENT_MUST_BE_PROGRAM_OR_OPTION`.

- L'établissement (`institutionId`) doit exister → sinon `INSTITUTION_NOT_FOUND` (`404`).
- Le groupe parent (`parentIdSiSco`) doit exister → sinon `GROUP_NOT_FOUND` (`404`).
- Ordre d'import : `PROGRAM`, puis `PROGRAM_OPTION`, puis `STUDENT_GROUP`.

### Exemple

```json
[
  {
    "name": "Licence Informatique",
    "idSiSco": "10000001",
    "institutionId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
    "codeSise": "11000001",
    "startDate": "2023-09-01",
    "endDate": "2026-08-31",
    "type": "PROGRAM",
    "parentIdSiSco": null
  },
  {
    "name": "Licence Informatique - Parcours IA",
    "idSiSco": "10000002",
    "institutionId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
    "codeSise": "11000002",
    "startDate": "2023-09-01",
    "endDate": "2025-08-31",
    "type": "PROGRAM_OPTION",
    "parentIdSiSco": "10000001"
  },
  {
    "name": "Groupe A",
    "idSiSco": "10000003",
    "institutionId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
    "codeSise": "11000003",
    "startDate": "2024-09-01",
    "endDate": "2025-06-30",
    "type": "STUDENT_GROUP",
    "parentIdSiSco": "10000002"
  }
]
```

---

## 4. `ExternalUserData` — Utilisateur externe

### Schéma

| Champ | Type JSON | Obligatoire | Description |
|---|---|---|---|
| `eppn` | string (≤255) | **Oui** | EduPersonPrincipalName, **unique** — clé d'upsert / mise à jour |
| `firstName` | string (≤255) | **Oui** | Prénom |
| `lastName` | string (≤255) | **Oui** | Nom |
| `email` | string (≤255) | **Oui** | Adresse e-mail valide |
| `categories` | array d'enums | **Oui** | `["STUDENT"]`, `["STAFF"]` ou `["STUDENT", "STAFF"]` |
| `externalId` | string (≤255) | **Oui** | Identifiant dans le SI source ; **unique par couple (`externalId`, `source`)** |
| `source` | enum | **Oui** | `PEGASE` \| `BACK_OFFICE` |
| `institutionId` | string (UUID) | **Oui** | **UUID** de l'établissement de rattachement |
| `groupId` | string (UUID) \| null | Non | **UUID** du groupe de rattachement |
| `status` | enum \| null | Non | `ACTIVE` \| `INACTIVE` \| `REMOVED` \| `BLOCKED` |

### Règles métier

- **`categories` est un tableau** (multi-catégories) et non une valeur unique. Les doublons sont
  ignorés (ensemble). S'il est absent ou `null`, l'utilisateur est créé **sans aucune
  catégorie** : renseignez-le toujours explicitement.
- `institutionId` doit correspondre à un établissement existant → `INSTITUTION_NOT_FOUND` (`404`).
- `groupId` est facultatif ; s'il est fourni, le groupe doit exister → `GROUP_NOT_FOUND` (`404`).
- `status` :
  - à la **création**, `null` ou absent ⇒ valeur par défaut `ACTIVE` ;
  - à la **mise à jour**, `null` ou absent ⇒ le statut courant est **conservé**.
- Le couple (`externalId`, `source`) est unique en base : réutiliser le même couple pour deux
  `eppn` différents provoque une erreur d'intégrité.
- `institutionId` / `groupId` attendent des **UUID**, pas le `hai` ni l'`idSiSco`.

### Exemple

```json
[
  {
    "eppn": "lucas.tessier@university.com",
    "firstName": "Lucas",
    "lastName": "Tessier",
    "email": "lucas.tessier@university.com",
    "categories": ["STUDENT"],
    "externalId": "PEG-0001",
    "source": "PEGASE",
    "institutionId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
    "groupId": "9b1deb4d-3b7d-4bad-9bdd-2b0d7b3dcb6d",
    "status": "ACTIVE"
  },
  {
    "eppn": "marie.dupont.staff@university.com",
    "firstName": "Marie",
    "lastName": "Dupont",
    "email": "marie.dupont@university.com",
    "categories": ["STAFF", "STUDENT"],
    "externalId": "TEACH-AG-83",
    "source": "BACK_OFFICE",
    "institutionId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
    "groupId": null,
    "status": null
  }
]
```

---

## 5. Ordre d'import recommandé

1. **Établissements** : `PRIMARY`, puis `SECONDARY`
2. **Groupes** : `PROGRAM`, puis `PROGRAM_OPTION`, puis `STUDENT_GROUP`
   (récupérer au préalable les UUID via `GET /back-office/admin/institutions`)
3. **Utilisateurs externes**
   (récupérer au préalable les UUID via `GET /back-office/admin/groups`)

### Exemple d'appel

```bash
curl -X POST http://localhost:8080/back-office/admin/institutions \
  -H "X-ADMIN-TOKEN: $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d @institutions.json
```

## 6. Codes de retour transverses

| Code | Cas |
|---|---|
| `200` | Import / mise à jour traité (voir le résumé pour le détail par élément) |
| `400` | Règle métier violée (hiérarchie parent/enfant, format de date invalide) |
| `403` | En-tête `X-ADMIN-TOKEN` absent ou incorrect |
| `404` | Établissement, groupe ou utilisateur référencé introuvable |
| `503` | Propriété `admin.token` non configurée côté serveur |

---

## Annexe — Jeux de données du seeder

Le seeder (`POST /back-office/seeder/reset`, également protégé par `X-ADMIN-TOKEN`) purge puis
recharge la base à partir des fichiers `src/main/resources/seeder/*.json`. **Leur schéma diffère
de celui des endpoints d'import** : comme les UUID sont regénérés à chaque exécution, les
fixtures référencent les entités par leur clé métier.

| Fichier | Différences avec le schéma d'import |
|---|---|
| `institutions.json` | Identique à `InstitutionData` |
| `groups.json` | `institutionHai` (HAI) **au lieu de** `institutionId` (UUID) |
| `external-users.json` | `institutionHai` et `groupIdSiSco` **au lieu de** `institutionId` et `groupId` |

Le seeder résout ces clés métier en UUID, puis applique exactement les mêmes règles métier que
les endpoints d'import (dont l'ordre `PROGRAM` → `PROGRAM_OPTION` → `STUDENT_GROUP`).

```json
// seeder/external-users.json
{
  "eppn": "lucas.tessier@university.com",
  "firstName": "Lucas",
  "lastName": "Tessier",
  "email": "lucas.tessier@university.com",
  "categories": ["STUDENT", "STAFF"],
  "externalId": "PEG-0001",
  "source": "BACK_OFFICE",
  "status": "ACTIVE",
  "institutionHai": "0350001A",
  "groupIdSiSco": "10000003"
}
```
