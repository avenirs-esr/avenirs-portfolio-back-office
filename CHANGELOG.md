# 📦 Changelog

This file tracks all notable changes to this repository, following
the [Keep a Changelog](https://keepachangelog.com/en/1.0.0/) format
and [Conventional Commits](https://www.conventionalcommits.org/) standard.

---


## [v1.0.7] - 2026-09-24

- **Terms of use upload**
    - New endpoint `POST back-office/cgu`, which takes a multipart `file` part and requires the `cgu:update` authority.
      It accepts an html file, hands it to the file feature of the api through the `FileClient` contract of
      `avenirs-portfolio-common` and returns the created `FileDTO`. Anything that is not `text/html` is rejected
      (#2683).
    - The call to the api relays the signed context of the logged-in user, so the api authenticates the same user and
      records it as the uploader of the file.
- **Update process**
    - New table `cgu`, holding the id of the file created by the api, an incremental version and the upload date. The
      version carries a unique constraint, so two concurrent publications cannot land on the same one.
    - New properties `avenirs.api.base-url` (default `http://localhost:10000`) and `avenirs.api.file.endpoint`
      (default `${avenirs.api.base-url}/storage`). They must point at the portfolio api of the environment.

## [v1.0.6] - 2026-09-11

- **Institution, group and program exposed to the other services**
    - New read endpoints `GET back-office/institutions/{id}`, `GET back-office/groups/{id}` and
      `GET back-office/groups/{id}/program`, protected by the API key alone. The admin endpoints under
      `back-office/admin` are unchanged.
    - The program endpoint walks the parent chain of a group up to its topmost ancestor, so a caller holding a student
      group resolves the program without knowing the hierarchy.
    - `EInstitutionType` and `EGroupType` moved to `avenirs-portfolio-common`, where the shared DTOs need them.
- **Update process**
    - Seeded institution and group ids are now derived from the `hai` and the `id_si_sco` instead of being random.
      Existing rows keep their current ids, since the seeder upserts on those keys. Recreate the database to pick up the
      derived ids, which the other services reference in their own seed data.

## [v1.0.5] - 2025-12-09
- Fix:
   - remove user package as it is no longer used.
   - add missing user service configuration.

## [v1.0.4] - 2025-12-04
- Configuration to use NoOp User Service

## [v1.0.3] - 2025-12-21
- Fix missing indices

## [v1.0.2] - 2025-10-16

- Settings for API Key.

## [v1.0.1] - 2025-10-01

- Fix:
    - spring security configuration.
    - some common objects moved to avenirs-portfolio-common repository.

## [v1.0.0] - 2025-09-23

- Initial version, the features were moved from avenirs-portfolio-api and reorganized.
