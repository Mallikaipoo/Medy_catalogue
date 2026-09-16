# MedyCatalog API

Spring Boot 3 student API. Java 17 toolchain. Flyway migrations in `src/main/resources/db/migration`.

```bash
cd ../infra
docker compose up -d
cd ../backend
./gradlew bootRun
./gradlew test
```

- Health: `http://localhost:8080/actuator/health`
- OpenAPI: `http://localhost:8080/swagger-ui.html`
- Base path: `/api/v1`
