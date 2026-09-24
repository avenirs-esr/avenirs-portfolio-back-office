# Règles d'import — Établissements, Groupes, Utilisateurs externes et Affiliations

Ce document décrit le corps (`body`) JSON attendu par les endpoints d'administration
d'import / mise à jour des entités structurantes du back-office :

- **`InstitutionData`** — établissement
- **`GroupData`** — groupe (formation, parcours, groupe d'étudiants)
- **`ExternalUserData`** — utilisateur externe (**identité seule**)
- **`ExternalUserAffiliationData`** — affiliation d'un utilisateur externe à un établissement
  et, éventuellement, à un groupe

> ⚠️ **Changement majeur** — Depuis la séparation des utilisateurs externes et de leurs
> affiliations, `ExternalUserData` **ne porte plus** `institutionId` ni `groupId`. Un
> utilisateur externe est désormais rattaché à **plusieurs** établissements et/ou groupes via
> des affiliations, importées par un endpoint dédié (§ 5). Voir § 5.6 pour la migration.

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
groupes et `eppn` pour les utilisateurs externes. Pour les affiliations, l'échec est identifié
par le **triplet** (`eppn`, `institutionHai`, `groupIdSiSco`) et le résumé expose
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

Ce payload ne décrit plus que **l'identité** de l'utilisateur. Ses rattachements
(établissements, groupes) sont importés séparément (§ 5).

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
| `status` | enum \| null | Non | `ACTIVE` \| `INACTIVE` \| `REMOVED` \| `BLOCKED` |

### Règles métier

- **`categories` est un tableau** (multi-catégories) et non une valeur unique. Les doublons sont
  ignorés (ensemble). S'il est absent ou `null`, l'utilisateur est créé **sans aucune
  catégorie** : renseignez-le toujours explicitement.
- `status` :
  - à la **création**, `null` ou absent ⇒ valeur par défaut `ACTIVE` ;
  - à la **mise à jour**, `null` ou absent ⇒ le statut courant est **conservé**.
- Le couple (`externalId`, `source`) est unique en base : réutiliser le même couple pour deux
  `eppn` différents provoque une erreur d'intégrité.
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
    "categories": ["STUDENT"],
    "externalId": "PEG-0001",
    "source": "PEGASE",
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
    "status": null
  }
]
```

### Lecture d'un utilisateur externe

Le DTO retourné par `GET /back-office/external-users[/{id}|/eppn/{eppn}]` expose désormais les
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
| Unicité | Le triplet (utilisateur, établissement, groupe) est unique en base |

Conséquences directes :

- Une affiliation « établissement seul » et une affiliation « établissement + groupe » sont
  **deux lignes distinctes**. Créer la seconde ne crée pas la première.
- Deux groupes différents du même établissement ⇒ deux affiliations.
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
| `institutionHai` | string | **Oui** | `hai` de l'établissement |
| `groupIdSiSco` | string \| null | Non | `idSiSco` du groupe ; `null` / absent ⇒ affiliation établissement seul |

```json
[
  { "eppn": "lucas.tessier@university.com", "institutionHai": "0350001A", "groupIdSiSco": "10000003" },
  { "eppn": "lucas.tessier@university.com", "institutionHai": "0330001C", "groupIdSiSco": null },
  { "eppn": "marie.dupont.staff@university.com", "institutionHai": "0350001A" }
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
      "createdAt": "2026-09-21T12:00:00Z"
    }
  ],
  "existing": [ { "id": "...", "externalUserId": "...", "institutionId": "...", "groupId": null, "createdAt": "..." } ],
  "failed": [
    {
      "eppn": "inconnu@university.com",
      "institutionHai": "0350001A",
      "groupIdSiSco": null,
      "message": "External user not found"
    }
  ]
}
```

Messages d'échec possibles : `External user not found`, `Institution not found`,
`Group not found`.

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
  "groupId": "9b1deb4d-3b7d-4bad-9bdd-2b0d7b3dcb6d"
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
| `INSTITUTION_NOT_FOUND` | `404` | `institutionHai` / `institutionId` inconnu |
| `GROUP_NOT_FOUND` | `404` | `groupIdSiSco` / `groupId` inconnu |
| `EXTERNAL_USER_AFFILIATION_NOT_FOUND` | `404` | Affiliation inconnue, ou n'appartenant pas à l'utilisateur ciblé |

Sur l'import en masse, ces erreurs ne remontent pas en HTTP : elles sont **capturées ligne à
ligne** et reportées dans `failed[]` avec leur message.

### 5.5 Suppressions et intégrité

- Supprimer un **utilisateur externe** supprime ses affiliations (`ON DELETE CASCADE`).
- Supprimer un **établissement** ou un **groupe** encore référencé par une affiliation échoue
  sur une violation de contrainte d'intégrité (`500`) : retirez d'abord les affiliations
  concernées.

### 5.6 Migration depuis l'ancien schéma

- Les champs `institutionId` et `groupId` ont disparu de `ExternalUserData`. Laissés dans un
  ancien payload, ils sont **ignorés silencieusement** (les propriétés inconnues ne font pas
  échouer la désérialisation) : l'utilisateur serait alors créé **sans aucune affiliation**.
  Adaptez vos scripts pour appeler l'import d'affiliations après celui des utilisateurs.
- Côté base, le changelog Liquibase `changelog_2026-09-21__12-00-00.xml` crée la table
  `external_user_affiliation`, **recopie** les couples `(institution_id, group_id)` existants de
  `external_user` en autant d'affiliations, puis **supprime ces deux colonnes**.
- Le DTO de lecture est passé de `institutionId` / `groupId` (UUID) à `institutionIds` /
  `groupIds` (listes d'UUID) : les consommateurs de l'API doivent être adaptés.

---

## 6. Ordre d'import recommandé

1. **Établissements** : `PRIMARY`, puis `SECONDARY`
2. **Groupes** : `PROGRAM`, puis `PROGRAM_OPTION`, puis `STUDENT_GROUP`
   (récupérer au préalable les UUID via `GET /back-office/admin/institutions`)
3. **Utilisateurs externes** (identité seule — aucun UUID nécessaire)
4. **Affiliations** — par clés métier (`eppn`, `hai`, `idSiSco`) : aucune récupération d'UUID
   n'est nécessaire à cette étape

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
recharge la base à partir des fichiers `src/main/resources/seeder/*.json`. **Leur schéma diffère
de celui des endpoints d'import** : comme les UUID sont regénérés à chaque exécution, les
fixtures référencent les entités par leur clé métier.

| Fichier | Différences avec le schéma d'import |
|---|---|
| `institutions.json` | Identique à `InstitutionData` |
| `groups.json` | `institutionHai` (HAI) **au lieu de** `institutionId` (UUID) |
| `external-users.json` | Identité seule : **plus aucune** référence à un établissement ou à un groupe |
| `external-user-affiliations.json` | **Une entrée par `eppn`**, avec deux listes de clés métier (schéma propre au seeder) |

Le seeder résout ces clés métier en UUID, puis applique exactement les mêmes règles métier que
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
  "source": "BACK_OFFICE",
  "status": "ACTIVE"
}
```

```json
// seeder/external-user-affiliations.json
{
  "eppn": "lucas.tessier@university.com",
  "institutionHais": ["0350001A", "0330001C"],
  "groupIdSiScos": ["10000003", "10000005"]
}
```

Règles de résolution propres à cette fixture :

- chaque `hai` de `institutionHais` produit une affiliation **sans groupe** ;
- chaque `idSiSco` de `groupIdSiScos` produit une affiliation vers ce groupe **et vers
  l'établissement porteur du groupe**, déduit automatiquement (inutile de le répéter dans
  `institutionHais`) ;
- les doublons sont absorbés par le « find or create » : l'exemple ci-dessus crée 4
  affiliations (2 établissements seuls + 2 groupes).

En mode `FAKER` (`seeder.source=FAKER`), les affiliations sont générées : les profils non
`STAFF` reçoivent une affiliation vers un groupe tiré au sort, les autres une affiliation
établissement, doublée aléatoirement d'une **seconde** affiliation vers un autre établissement —
de quoi exercer les cas multi-établissements.
