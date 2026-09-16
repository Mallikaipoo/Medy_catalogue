# Testing strategy

## Unit (backend)

Scoring, negative marking, streak (server date), XP/level, study-plan rules, question filters, entitlement checks, JWT issue/parse, password rules.

Phase 2 covers JWT, auth validation, and controller contracts with mocked services.

## Unit (Flutter)

Riverpod notifiers, error mapping, timer display (not official remaining time).

## API tests

Spring MockMvc / later REST-assured: auth, practice submit, **answer-key leak regression**, subscription verify stubs, leaderboard privacy.

## Widget tests

Home, question player, result, login.

## Integration journey

Register → select exam → practice → submit → result → review.

## Security

Admin authz, rate limit, token expiry, webhook signatures.

## Performance (Phase 12)

Paged question lists, attempt insert under load.

## How to run (now)

```bash
cd backend
./gradlew test
```

```bash
cd mobile
flutter test
```
