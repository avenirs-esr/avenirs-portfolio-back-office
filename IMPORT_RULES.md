# Règles d'import — Établissements, Groupes, Utilisateurs externes et Affiliations

Ce document décrit le corps (`body`) JSON attendu par les endpoints d'administration
d'import / mise à jour des entités structurantes du back-office :

- **`InstitutionData`** — établissement
- **`GroupData`** — groupe (formation, parcours, groupe d'étudiants)
- **`ExternalUserData`** — utilisateur externe (**identité seule**)
- **`ExternalUserAffiliationData`** — affiliation d'un utilisateur externe à un établissement
  et, éventuellement, à un groupe

## Sommaire

- [1. Généralités](#1-généralités)
  - [Endpoints](#endpoints)
  - [Sémantique POST vs PUT](#sémantique-post-vs-put)
- [2. `InstitutionData` — Établissement](#2-institutiondata--établissement)
  - [Schéma](#schéma)
  - [Règles métier](#règles-métier)
  - [Exemple](#exemple)
- [3. `GroupData` — Groupe](#3-groupdata--groupe)
  - [Schéma](#schéma-1)
  - [3.1 Types de groupe (`EGroupType`)](#31-types-de-groupe-egrouptype)
  - [Règles métier (hiérarchie par `type`)](#règles-métier-hiérarchie-par-type)
  - [Exemple](#exemple-1)
- [4. `ExternalUserData` — Utilisateur externe](#4-externaluserdata--utilisateur-externe)
  - [Schéma](#schéma-2)
  - [Règles métier](#règles-métier-1)
  - [Exemple](#exemple-2)
  - [Lecture d'un utilisateur externe](#lecture-dun-utilisateur-externe)
- [5. `ExternalUserAffiliationData` — Affiliation](#5-externaluseraffiliationdata--affiliation)
  - [5.1 Modèle](#51-modèle)
  - [5.2 Import en masse — `POST /back-office/external-user-affiliations`](#52-import-en-masse--post-back-officeexternal-user-affiliations)
    - [Schéma du corps](#schéma-du-corps)
    - [Comportement](#comportement)
    - [Réponse (`200`)](#réponse-200)
  - [5.3 Endpoints unitaires (par UUID)](#53-endpoints-unitaires-par-uuid)
  - [5.4 Codes d'erreur](#54-codes-derreur)
  - [5.5 Suppressions et intégrité](#55-suppressions-et-intégrité)
- [6. Ordre d'import recommandé](#6-ordre-dimport-recommandé)
  - [Exemple d'appel](#exemple-dappel)
- [7. Codes de retour transverses](#7-codes-de-retour-transverses)
- [Annexe — Jeux de données du seeder](#annexe--jeux-de-données-du-seeder)

## 1. Généralités

| Point | Valeur |
|---|---|
| Authentification | En-tête `X-ADMIN-TOKEN: <admin.token>` obligatoire sur les endpoints d'import en masse |
| Content-Type | `application/json` |
| Corps | **Toujours un tableau JSON** (`[ { ... }, { ... } ]`), même pour un seul élément |
| Format des dates | ISO `yyyy-MM-dd` (ex. `"2024-09-01"`) |
| Énumérations | Sérialisées par leur **nom exact en MAJUSCULES** (`PROGRAM`, `PRIMARY`, `STUDENT`…) |
| Champs optionnels | Peuvent être omis ou valorisés à `null` |
| Format d'erreur | `{ "code": "<CODE>", "message": "<message>" }` |

### Endpoints

| Ressource | POST (import / upsert) | PUT (mise à jour seule) | Permission requise |
|---|---|---|---|
| Établissement | `POST /back-office/admin/institutions` | `PUT /back-office/admin/institutions` | `primary-establishment:*` / `secondary-establishment:*` |
| Groupe | `POST /back-office/admin/groups` | `PUT /back-office/admin/groups` | `group:import` / `group:update` |
| Utilisateur externe | `POST /back-office/external-users` | `PUT /back-office/external-users` | `external-user:import` / `external-user:update` |
| Affiliation (masse) | `POST /back-office/external-user-affiliations` | — | `external-user-affiliation:import` |

Les endpoints **unitaires** d'affiliation (§ 5.3) sont montés sous
`/back-office/external-users/{externalUserId}/affiliations` : ils sont protégés par les
permissions `external-user:read` / `external-user:update` **sans** `X-ADMIN-TOKEN`.

### Sémantique POST vs PUT

**POST = upsert tolérant.** Chaque élément est créé, ou mis à jour s'il existe déjà (recherche
sur la clé métier : `uai`, `idSISco`, `eppn`). Les éléments en erreur métier n'interrompent pas
le traitement : ils sont retournés dans `failed[]`. Réponse :

```json
{
  "createdCount": 2,
  "updatedCount": 1,
  "failedCount": 1,
  "created": [ ... ],
  "updated": [ ... ],
  "failed": [ { "uai": "0350009Z", "message": "Institution not found" } ]
}
```

La clé identifiant l'élément en échec est `uai` pour les établissements, `idSISco` pour les
groupes et `eppn` pour les utilisateurs externes. Pour les affiliations, l'échec est identifié
par le **triplet** (`eppn`, `institutionUAI`, `groupIdSISco`) et le résumé expose
`existingCount` / `existing[]` au lieu de `updatedCount` / `updated[]` (§ 5.2).

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
| `sigle` | string \| null | Non | Sigle / nom court de l'établissement (ex. `UR`) |
| `uai` | string | **Oui** | Identifiant UAI, **unique** — sert de clé d'upsert / mise à jour |
| `siret` | string | **Oui** | Numéro SIRET (14 chiffres) |
| `type` | enum | **Oui** | `PRIMARY` \| `SECONDARY` |
| `parentUAI` | string \| null | Conditionnel | `uai` de l'établissement parent |

### Règles métier

- `type = PRIMARY` → `parentUAI` **doit être `null` / absent**
  (sinon `INSTITUTION_PRIMARY_CANNOT_HAVE_PARENT`, `400`).
- `type = SECONDARY` → `parentUAI` **est obligatoire**
  (sinon `INSTITUTION_SECONDARY_REQUIRES_PARENT`, `400`).
- Le parent référencé par `parentUAI` doit exister (`INSTITUTION_NOT_FOUND`, `404`) et être de
  type `PRIMARY` (`INSTITUTION_PARENT_MUST_BE_PRIMARY`, `400`).
- Hiérarchie sur **2 niveaux uniquement** : un `SECONDARY` ne peut pas être parent.
- Ordre d'import : importer les `PRIMARY` **avant** les `SECONDARY` qui les référencent.

### Exemple

```json
[
  {
    "name": "Université de Rennes",
    "sigle": "UR",
    "uai": "0350001A",
    "siret": "13000550100015",
    "type": "PRIMARY",
    "parentUAI": null
  },
  {
    "name": "Université de Rennes - IUT",
    "sigle": "IUT-R",
    "uai": "0350002B",
    "siret": "13000550100023",
    "type": "SECONDARY",
    "parentUAI": "0350001A"
  }
]
```

---

## 3. `GroupData` — Groupe

### Schéma

| Champ | Type JSON | Obligatoire | Description |
|---|---|---|---|
| `name` | string | **Oui** | Libellé du groupe |
| `idSISco` | string | **Oui** | Identifiant SI Scolarité, **unique** — clé d'upsert / mise à jour |
| `institutionUAI` | string | **Oui** | `uai` de l'établissement de rattachement |
| `codeSise` | string \| null | Non | Code SISE |
| `startDate` | string (`yyyy-MM-dd`) \| null | Non | Date de début — voir la note ci-dessous |
| `endDate` | string (`yyyy-MM-dd`) \| null | Non | Date de fin — voir la note ci-dessous |
| `type` | enum | **Oui** | `PROGRAM` \| `PROGRAM_OPTION` \| `STUDENT_GROUP` — voir § 3.1 |
| `parentIdSISco` | string \| null | Conditionnel | `idSISco` du groupe parent |

> 💡 `startDate` / `endDate` sont facultatives mais **utiles à renseigner** : elles portent le
> cycle de vie du groupe — la période pendant laquelle la formation, l'option ou le groupe
> d'étudiants est active. Les renseigner permet de distinguer un groupe en cours d'un groupe
> clos sans avoir à le supprimer, et de filtrer dessus via
> `GET /back-office/admin/groups?startDate=…&endDate=…` (`startDate` ⇒ groupes commençant à
> cette date ou après, `endDate` ⇒ groupes finissant à cette date ou avant). Un groupe dont la
> date est laissée à `null` est **exclu** du filtre correspondant.

### 3.1 Types de groupe (`EGroupType`)

| Valeur | Signification | Place dans la hiérarchie |
|---|---|---|
| `PROGRAM` | **Formation** (ex. « Licence Informatique ») | Racine : pas de parent |
| `PROGRAM_OPTION` | **Option / parcours de formation** (ex. « Parcours IA ») | Enfant d'un `PROGRAM` |
| `STUDENT_GROUP` | **Groupe d'étudiants** (ex. « Groupe A », une promotion, un TD) | Enfant d'un `PROGRAM` ou d'un `PROGRAM_OPTION` |

### Règles métier (hiérarchie par `type`)

| `type` | `parentIdSISco` | Type du parent exigé |
|---|---|---|
| `PROGRAM` | **doit être `null`** | — |
| `PROGRAM_OPTION` | **obligatoire** | `PROGRAM` |
| `STUDENT_GROUP` | **obligatoire** | `PROGRAM` ou `PROGRAM_OPTION` |

Codes d'erreur associés (`400`) : `GROUP_PROGRAM_CANNOT_HAVE_PARENT`,
`GROUP_PROGRAM_OPTION_REQUIRES_PARENT`, `GROUP_PROGRAM_OPTION_PARENT_MUST_BE_PROGRAM`,
`GROUP_STUDENT_GROUP_REQUIRES_PARENT`, `GROUP_STUDENT_GROUP_PARENT_MUST_BE_PROGRAM_OR_OPTION`.

- L'établissement (`institutionUAI`) doit exister → sinon `INSTITUTION_NOT_FOUND` (`404`).
- Le groupe parent (`parentIdSISco`) doit exister → sinon `GROUP_NOT_FOUND` (`404`).
- Ordre d'import : `PROGRAM`, puis `PROGRAM_OPTION`, puis `STUDENT_GROUP`.

### Exemple

```json
[
  {
    "name": "Licence Informatique",
    "idSISco": "10000001",
    "institutionUAI": "0350001A",
    "codeSise": "11000001",
    "startDate": "2023-09-01",
    "endDate": "2026-08-31",
    "type": "PROGRAM",
    "parentIdSISco": null
  },
  {
    "name": "Licence Informatique - Parcours IA",
    "idSISco": "10000002",
    "institutionUAI": "0350001A",
    "codeSise": "11000002",
    "startDate": "2023-09-01",
    "endDate": "2025-08-31",
    "type": "PROGRAM_OPTION",
    "parentIdSISco": "10000001"
  },
  {
    "name": "Groupe A",
    "idSISco": "10000003",
    "institutionUAI": "0350001A",
    "codeSise": "11000003",
    "startDate": "2024-09-01",
    "endDate": "2025-06-30",
    "type": "STUDENT_GROUP",
    "parentIdSISco": "10000002"
  }
]
```

---

## 4. `ExternalUserData` — Utilisateur externe

Ce payload décrit **l'identité** de l'utilisateur. Ses rattachements (établissements, groupes)
sont importés séparément (§ 5).

### Schéma

| Champ | Type JSON | Obligatoire | Description |
|---|---|---|---|
| `eppn` | string (≤255) | **Oui** | EduPersonPrincipalName, **unique** — clé d'upsert / mise à jour |
| `firstName` | string (≤255) | **Oui** | Prénom |
| `lastName` | string (≤255) | **Oui** | Nom |
| `email` | string (≤255) | **Oui** | Adresse e-mail valide |
| `roles` | array d'enums | **Oui** | `TEACHER`, `SUIO` et/ou `STUDENT` (ex. `["TEACHER"]`, `["TEACHER", "STUDENT"]`) |
| `externalId` | string (≤255) | **Oui** | Identifiant dans le SI d'origine, **unique** |
| `institutionUAI` | string (≤255) | **Oui** | `uai` de l'établissement d'origine de l'utilisateur |

Valeurs possibles de `roles` :

| Rôle | Signification |
|---|---|
| `TEACHER` | Enseignant |
| `SUIO` | Personnel du service universitaire d'information et d'orientation |
| `STUDENT` | Étudiant |

### Règles métier

- **`roles` est un tableau** (multi-rôles) et non une valeur unique : un utilisateur peut être
  déclaré `TEACHER` **et** `STUDENT`. Les doublons sont ignorés (ensemble). S'il est absent ou
  `null`, l'utilisateur est créé **sans aucun rôle** : renseignez-le toujours explicitement.
- `TEACHER` et `SUIO` ouvrent les mêmes droits : les déclarer tous les deux est accepté, mais
  équivaut à n'en déclarer qu'un.
- Toute valeur autre que `TEACHER`, `SUIO` ou `STUDENT` est rejetée en `400` à la lecture du
  corps.
- Un utilisateur importé est créé **actif** (`status = ACTIVE`). L'import ne touche jamais au
  statut d'un utilisateur déjà existant : il reste celui en base.
- `externalId` est unique en base : le réutiliser pour deux `eppn` différents provoque une
  erreur d'intégrité.
- `institutionUAI` enregistre l'établissement d'origine de l'utilisateur. C'est une donnée
  d'identité, **distincte des affiliations** (§ 5) qui portent, elles, les droits d'accès : un
  utilisateur peut être affilié à d'autres établissements que celui de son `institutionUAI`.
- **Aucune affiliation n'est créée** par cet import : un utilisateur importé ici n'est rattaché
  à aucun établissement tant que ses affiliations n'ont pas été importées (§ 5).
- Un `PUT` ne touche **jamais** aux affiliations existantes : elles ne sont modifiables que par
  les endpoints d'affiliation.
- Supprimer un utilisateur externe (`DELETE /back-office/external-users/{id}`) supprime **en
  cascade** toutes ses affiliations.

### Exemple

```json
[
  {
    "eppn": "lucas.tessier@university.com",
    "firstName": "Lucas",
    "lastName": "Tessier",
    "email": "lucas.tessier@university.com",
    "roles": ["STUDENT"],
    "externalId": "PEG-0001",
    "institutionUAI": "0350001A"
  },
  {
    "eppn": "marie.dupont.staff@university.com",
    "firstName": "Marie",
    "lastName": "Dupont",
    "email": "marie.dupont@university.com",
    "roles": ["TEACHER", "STUDENT"],
    "externalId": "TEACH-AG-83",
    "institutionUAI": "0350001A"
  }
]
```

### Lecture d'un utilisateur externe

Le DTO retourné par `GET /back-office/external-users[/{id}|/eppn/{eppn}]` expose les
rattachements **sous forme de listes**, reconstituées à partir des affiliations :

```json
{
  "eppn": "lucas.tessier@university.com",
  "firstName": "Lucas",
  "lastName": "Tessier",
  "email": "lucas.tessier@university.com",
  "categories": ["STUDENT", "STAFF"],
  "externalId": "PEG-0001",
  "source": "BACK_OFFICE",
  "institutionIds": ["3fa85f64-...", "7c9e6679-..."],
  "groupIds": ["9b1deb4d-..."],
  "status": "ACTIVE"
}
```

- `source` : toujours `BACK_OFFICE`, l'import étant la seule origine des utilisateurs externes.
- `institutionIds` : établissements de **toutes** les affiliations, dédoublonnés.
- `groupIds` : groupes des affiliations qui en portent un (les affiliations « établissement
  seul » n'y contribuent pas).
- Les filtres `GET /back-office/external-users?institutionId=…&groupId=…` s'appuient sur les
  affiliations : un utilisateur est retourné s'il possède **au moins une** affiliation
  correspondante. Les deux filtres sont évalués **indépendamment** — ils peuvent être
  satisfaits par deux affiliations différentes du même utilisateur.

---

## 5. `ExternalUserAffiliationData` — Affiliation

### 5.1 Modèle

Une affiliation est un lien **(utilisateur externe, établissement, groupe facultatif)**. Un
utilisateur externe peut en porter **autant que nécessaire** : plusieurs établissements,
plusieurs formations, ou les deux.

| Élément | Règle |
|---|---|
| Utilisateur externe | **Obligatoire** — doit exister |
| Établissement | **Obligatoire** — doit exister |
| Groupe | **Facultatif** (`null` ⇒ affiliation « établissement seul ») — doit exister s'il est fourni |
| Catégorie | **Obligatoire** — `STUDENT` ou `STAFF`, et doit correspondre à un rôle déclaré pour cet utilisateur à l'import (§ 4) |
| Unicité | Le quadruplet (utilisateur, établissement, groupe, catégorie) est unique en base |

Conséquences directes :

- Une affiliation « établissement seul » et une affiliation « établissement + groupe » sont
  **deux lignes distinctes**. Créer la seconde ne crée pas la première.
- Deux groupes différents du même établissement ⇒ deux affiliations.
- Un utilisateur à la fois `STUDENT` et `STAFF` sur le même périmètre ⇒ deux affiliations, une
  par catégorie : c'est la catégorie de l'affiliation qui détermine les droits appliqués.
- **Aucun contrôle de cohérence** n'est fait entre l'établissement fourni et l'établissement
  porteur du groupe : c'est à l'appelant de fournir le couple cohérent.

### 5.2 Import en masse — `POST /back-office/external-user-affiliations`

Destiné aux établissements qui importent leurs affiliations par lot (export CSV, par exemple)
**sans connaître les UUID internes** : tout est référencé par clé métier.

En-têtes : `X-ADMIN-TOKEN` + permission `external-user-affiliation:import`.

#### Schéma du corps

| Champ | Type JSON | Obligatoire | Description |
|---|---|---|---|
| `eppn` | string | **Oui** | `eppn` de l'utilisateur externe (doit déjà être importé) |
| `institutionUAI` | string | **Oui** | `uai` de l'établissement |
| `groupIdSISco` | string \| null | Non | `idSISco` du groupe ; `null` / absent ⇒ affiliation établissement seul |
| `category` | enum | **Oui** | `STUDENT` ou `STAFF` — doit correspondre à un rôle déclaré pour cet utilisateur à l'import (§ 4) |

```json
[
  { "eppn": "lucas.tessier@university.com", "institutionUAI": "0350001A", "groupIdSISco": "10000003", "category": "STUDENT" },
  { "eppn": "lucas.tessier@university.com", "institutionUAI": "0330001C", "groupIdSISco": null, "category": "STUDENT" },
  { "eppn": "marie.dupont.staff@university.com", "institutionUAI": "0350001A", "category": "STAFF" }
]
```

#### Comportement

- **Tolérant aux erreurs** : chaque ligne est traitée indépendamment, les erreurs métier
  atterrissent dans `failed[]` sans interrompre le lot.
- **Idempotent** : une affiliation déjà présente n'est **pas dupliquée**, elle est renvoyée dans
  `existing[]`. Rejouer le même fichier est sans effet de bord.
- **Additif, jamais destructif** : l'import n'est pas une synchronisation. Les affiliations
  absentes du payload sont **conservées** ; pour en retirer une, utilisez le `DELETE`
  unitaire (§ 5.3).

#### Réponse (`200`)

```json
{
  "createdCount": 2,
  "existingCount": 1,
  "failedCount": 1,
  "created": [
    {
      "id": "1e4a...",
      "externalUserId": "b3f2...",
      "institutionId": "3fa85f64-...",
      "groupId": "9b1deb4d-...",
      "category": "STUDENT",
      "createdAt": "2026-09-21T12:00:00Z"
    }
  ],
  "existing": [ { "id": "...", "externalUserId": "...", "institutionId": "...", "groupId": null, "category": "STAFF", "createdAt": "..." } ],
  "failed": [
    {
      "eppn": "inconnu@university.com",
      "institutionUAI": "0350001A",
      "groupIdSISco": null,
      "category": "STUDENT",
      "message": "External user not found"
    }
  ]
}
```

Messages d'échec possibles : `External user not found`, `Institution not found`,
`Group not found`, `The affiliation category must be one of the external user's categories`.

### 5.3 Endpoints unitaires (par UUID)

Ces endpoints ne demandent **pas** `X-ADMIN-TOKEN`, uniquement les permissions indiquées.

| Méthode | URL | Permission | Réponse |
|---|---|---|---|
| `GET` | `/back-office/external-users/{externalUserId}/affiliations` | `external-user:read` | `200` + liste d'affiliations |
| `POST` | `/back-office/external-users/{externalUserId}/affiliations` | `external-user:update` | `201` + l'affiliation |
| `DELETE` | `/back-office/external-users/{externalUserId}/affiliations/{affiliationId}` | `external-user:update` | `204` |

Corps du `POST` (UUID, pas de clés métier) :

```json
{
  "institutionId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
  "groupId": "9b1deb4d-3b7d-4bad-9bdd-2b0d7b3dcb6d",
  "category": "STUDENT"
}
```

- Le `POST` unitaire est **idempotent** lui aussi : si l'affiliation existe déjà, l'existante
  est renvoyée — toujours avec le statut `201`, sans création de doublon.
- Le `DELETE` vérifie que l'affiliation **appartient bien** à l'utilisateur de l'URL ; sinon
  `EXTERNAL_USER_AFFILIATION_NOT_FOUND` (`404`), au même titre qu'un identifiant inconnu.
- Le `GET` renvoie `EXTERNAL_USER_NOT_FOUND` (`404`) si l'utilisateur n'existe pas (et non une
  liste vide).

### 5.4 Codes d'erreur

| Code | HTTP | Cas |
|---|---|---|
| `EXTERNAL_USER_NOT_FOUND` | `404` | `eppn` / `externalUserId` inconnu |
| `INSTITUTION_NOT_FOUND` | `404` | `institutionUAI` / `institutionId` inconnu |
| `GROUP_NOT_FOUND` | `404` | `groupIdSISco` / `groupId` inconnu |
| `EXTERNAL_USER_AFFILIATION_NOT_FOUND` | `404` | Affiliation inconnue, ou n'appartenant pas à l'utilisateur ciblé |
| `EXTERNAL_USER_AFFILIATION_CATEGORY_NOT_ALLOWED` | `400` | `category` absente, ou absente des catégories de l'utilisateur |

Sur l'import en masse, ces erreurs ne remontent pas en HTTP : elles sont **capturées ligne à
ligne** et reportées dans `failed[]` avec leur message.

### 5.5 Suppressions et intégrité

- Supprimer un **utilisateur externe** supprime ses affiliations (`ON DELETE CASCADE`).
- Supprimer un **établissement** ou un **groupe** encore référencé par une affiliation échoue
  sur une violation de contrainte d'intégrité (`500`) : retirez d'abord les affiliations
  concernées.

---

## 6. Ordre d'import recommandé

1. **Établissements** : `PRIMARY`, puis `SECONDARY`
2. **Groupes** : `PROGRAM`, puis `PROGRAM_OPTION`, puis `STUDENT_GROUP`
3. **Utilisateurs externes**
4. **Affiliations**

Tout l'import se fait par clés métier (`uai`, `idSISco`, `eppn`) : aucun UUID interne n'est à
récupérer entre deux étapes.

### Exemple d'appel

```bash
curl -X POST http://localhost:8080/back-office/admin/institutions \
  -H "X-ADMIN-TOKEN: $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d @institutions.json

curl -X POST http://localhost:8080/back-office/external-users \
  -H "X-ADMIN-TOKEN: $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d @external-users.json

curl -X POST http://localhost:8080/back-office/external-user-affiliations \
  -H "X-ADMIN-TOKEN: $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d @external-user-affiliations.json
```

## 7. Codes de retour transverses

| Code | Cas |
|---|---|
| `200` | Import / mise à jour traité (voir le résumé pour le détail par élément) |
| `201` | Affiliation créée (ou déjà existante) via le `POST` unitaire |
| `204` | Affiliation supprimée |
| `400` | Règle métier violée (hiérarchie parent/enfant, format de date invalide) |
| `403` | En-tête `X-ADMIN-TOKEN` absent ou incorrect |
| `404` | Établissement, groupe, utilisateur ou affiliation référencé introuvable |
| `503` | Propriété `admin.token` non configurée côté serveur |

---

## Annexe — Jeux de données du seeder

Le seeder (`POST /back-office/seeder/reset`, également protégé par `X-ADMIN-TOKEN`) purge puis
recharge la base à partir des fichiers `src/main/resources/seeder/*.json`. Ces fixtures suivent
le même schéma que les endpoints d'import, à quelques exceptions près :

| Fichier | Écart avec le schéma d'import |
|---|---|
| `institutions.json` | Identique à `InstitutionData` |
| `groups.json` | Identique à `GroupData` |
| `external-users.json` | `ExternalUserData` avec des `categories` au lieu des `roles` (la fixture alimente directement le modèle interne) + un `status` facultatif (elle peut créer des comptes inactifs) |
| `external-user-affiliations.json` | **Une entrée par couple (`eppn`, `category`)**, avec deux listes de clés métier |

Le seeder résout les clés métier en UUID, puis applique exactement les mêmes règles métier que
les endpoints d'import (dont l'ordre `PROGRAM` → `PROGRAM_OPTION` → `STUDENT_GROUP`). L'ordre
d'exécution est : établissements → configs → groupes → utilisateurs externes → affiliations ; la
purge se fait dans l'ordre inverse (les affiliations d'abord).

```json
// seeder/external-users.json
{
  "eppn": "lucas.tessier@university.com",
  "firstName": "Lucas",
  "lastName": "Tessier",
  "email": "lucas.tessier@university.com",
  "categories": ["STUDENT", "STAFF"],
  "externalId": "PEG-0001",
  "institutionUAI": "0350001A",
  "status": "ACTIVE"
}
```

```json
// seeder/external-user-affiliations.json
{
  "eppn": "lucas.tessier@university.com",
  "category": "STUDENT",
  "institutionUAIs": ["0350001A", "0330001C"],
  "groupIdSIScos": ["10000003", "10000005"]
}
```

Règles de résolution propres à cette fixture :

- chaque `uai` de `institutionUAIs` produit une affiliation **sans groupe** ;
- chaque `idSISco` de `groupIdSIScos` produit une affiliation vers ce groupe **et vers
  l'établissement porteur du groupe**, déduit automatiquement (inutile de le répéter dans
  `institutionUAIs`) ;
- toutes les affiliations de l'entrée portent sa `category` ; un utilisateur à la fois
  `STUDENT` et `STAFF` a donc **deux entrées** dans la fixture ;
- les doublons sont absorbés par le « find or create » : l'exemple ci-dessus crée 4
  affiliations (2 établissements seuls + 2 groupes).

En mode `FAKER` (`seeder.source=FAKER`), les affiliations sont générées : chaque profil
`STUDENT` reçoit une affiliation vers un groupe tiré au sort, chaque profil `STAFF` une
affiliation établissement, doublée aléatoirement d'une **seconde** affiliation vers un autre
établissement — de quoi exercer les cas multi-établissements.
