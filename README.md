# MedyCatalog

**Learn. Practice. Compete. Crack Your Exam.**

Exam-preparation platform for competitive examinations in India. Launch exams: JEE, NEET, NDA. Additional examinations are data, not new app builds.

This repository is a monorepo:

| Path | Surface |
| --- | --- |
| `mobile/` | Flutter Android + iOS student app |
| `backend/` | Spring Boot 3 REST API (`/api/v1`) |
| `admin/` | Next.js administration console |
| `infra/` | Local Docker Compose (PostgreSQL, Redis, MinIO) |
| `docs/` | Architecture, ERD, API, environments, phase reports |

Flutter never connects to PostgreSQL. Secrets never live in the mobile binary.

## Current phase

**Phase 10 — Monetization** is implemented (yearly ₹1,499 default, server-side Premium, ads never on the player).

| Phase | Status |
| --- | --- |
| 1 Architecture | Complete — see `docs/` |
| 2 Authentication | Complete |
| 3 Catalog | Complete |
| 4 Practice | Complete |
| 10 Monetization | Complete |
| 5–9, 11–14 | Playbooks ready |

## Prerequisites

- Java 17+ (Java 21 preferred when installed)
- Docker Desktop (PostgreSQL 16, Redis 7, MinIO)
- Flutter stable (for `mobile/`)
- Node.js 20+ (for `admin/`)

## Quick start (API)

```bash
cd infra
docker compose up -d

cd ../backend
./gradlew bootRun
```

API base URL: `http://localhost:8080/api/v1`

OpenAPI UI: `http://localhost:8080/swagger-ui.html`

Copy `infra/env.example` values. Do not use the production database.

## Quick start (Flutter)

Install the Flutter SDK, then:

```bash
cd mobile
flutter create . --org in.claris --project-name medycatalog --platforms android,ios
flutter pub get
flutter run --dart-define=API_BASE_URL=http://10.0.2.2:8080
```

On a physical device, replace `10.0.2.2` with your machine LAN IP.

## Application identifiers

- Android application ID: `in.claris.medycatalog`
- iOS bundle ID: `in.claris.medycatalog`

Signing keys and store credentials are not stored in Git.

## Documentation index

- [Architecture](docs/ARCHITECTURE.md)
- [Database / ERD](docs/ERD.md)
- [Table catalog](docs/DATABASE.md)
- [API](docs/API.md)
- [OpenAPI](docs/openapi.yaml)
- [Environments](docs/ENVIRONMENTS.md)
- [Security](docs/SECURITY.md)
- [Authentication](docs/AUTHENTICATION.md)
- [Phases](docs/PHASES.md)

## Rules that do not change

- Scores, XP, streaks, rewards, and subscription status are server-side.
- Correct answers are not returned until the student has submitted.
- AI-generated questions stay draft until human review.
- Exam rules and prices are database-driven.
- Do not promise exam results in store listing or marketing copy.
