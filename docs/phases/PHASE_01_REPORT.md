# Phase 1 report — Architecture

- Date: 2026-09-16
- Goal: Technical architecture, database ERD, API specification, Flutter and backend project structure. No student exam UI.

## Completed

- New Git repository at `C:\Users\lko92\JAVA\medycatalog` (not inside filemaker-pos).
- Architecture, ERD, table catalog, API list, OpenAPI outline, environments, security, hosting, auth, subscription, AI, admin, testing, store plans.
- Monorepo folders: `docs/`, `infra/`, `backend/`, `mobile/`, `admin/`.
- Local Compose for PostgreSQL, Redis, MinIO.

## Files created or modified

- Root: `README.md`, `CHANGELOG.md`, `.gitignore`
- `docs/**`
- `infra/docker-compose.yml`, `infra/env.example`
- Backend and mobile skeletons (auth implementation is Phase 2)

## Database changes

None applied yet. Identity schema lands in Phase 2 Flyway `V1__identity.sql`.

## APIs added

Specified in `docs/API.md` and `docs/openapi.yaml`. Not served until Phase 2.

## Tests added

None in Phase 1.

## Known issues

- Flutter SDK and Node.js are not on the Windows PATH on the development machine; `flutter create` / `npm install` must be run after those SDKs are installed.
- Host JDK is 17; Gradle toolchain is 17. Java 21 can be adopted later without an API rewrite.

## How to test

Read `docs/ARCHITECTURE.md` and confirm the ERD matches `docs/DATABASE.md`. Start Compose:

```bash
cd infra
docker compose up -d
docker compose ps
```

## Approval

Approved with the architecture plan. Phase 2 authentication follows in this repository.
