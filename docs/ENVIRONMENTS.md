# Environments

Never point a development app at the production database.

| Name | API | Database | Redis | Mobile flavors |
| --- | --- | --- | --- | --- |
| development | localhost:8080 | Docker Compose `medycatalog` | Compose Redis | `dev` |
| staging | staging API host | dedicated RDS | dedicated | `staging` |
| production | api.medycatalog (TBD) | Multi-AZ RDS ap-south-1 | ElastiCache | `prod` |

## Files

| File | Committed | Purpose |
| --- | --- | --- |
| `infra/env.example` | yes | Names of variables only |
| `infra/.env` | **no** | Local Compose secrets |
| `backend/src/main/resources/application.yml` | yes | Non-secret defaults |
| `backend/src/main/resources/application-dev.yml` | yes | Local profile |
| `backend/src/main/resources/application-prod.yml` | yes | Prod requires env injection |

## Required secrets (inject, do not commit)

- `DATABASE_URL`, `DATABASE_USER`, `DATABASE_PASSWORD`
- `MEDYCATALOG_JWT_SECRET` (32+ random bytes)
- `GOOGLE_OAUTH_CLIENT_ID` (and iOS client ID if split)
- `APPLE_BUNDLE_ID` / Apple audience
- Later: AI provider key, Play service account, App Store key, SMTP/SMS, S3 keys

Flutter may contain only:

- API base URL per flavor
- OAuth **client IDs** (public)
- AdMob app IDs (public)

Never: database password, JWT secret, AI keys, Play/App Store private keys.

## Local Compose

```bash
cd infra
copy env.example .env   # Windows
docker compose up -d
```

Postgres is published on `localhost:5432` for this Windows development host. Production must not publish database ports to the public internet.
