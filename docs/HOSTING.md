# Hosting architecture

## Local

Docker Compose: PostgreSQL 16, Redis 7, MinIO. API on port 8080.

Flutter emulator: `http://10.0.2.2:8080`. Physical device: machine LAN IP.

## Production (India latency)

Preferred region: AWS `ap-south-1` (Mumbai) or GCP `asia-south1`.

| Component | Choice |
| --- | --- |
| Edge | ALB / HTTPS |
| API | ECS Fargate or Compute Engine, 2+ stateless tasks |
| DB | RDS PostgreSQL Multi-AZ, daily backups + PITR |
| Cache | ElastiCache Redis |
| Files | S3 + CloudFront |
| Secrets | AWS Secrets Manager |
| Workers | Separate service when import/AI/leaderboard load grows |

Monitor CPU, RAM, disk, API latency, error rate, DB connections.

Automated backups are mandatory before store production.

## CI

GitHub Actions: `./gradlew test` in `backend/`. Flutter tests added when the SDK is available on agents.
