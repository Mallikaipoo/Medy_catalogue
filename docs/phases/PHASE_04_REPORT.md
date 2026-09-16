# Phase 4 report — Practice engine

- Date: 2026-09-16
- Goal: Start session, answer without reveal, server score, timer, results, review.

## Completed

- Flyway `V3__practice.sql`.
- `ScoringCalculator` for MCQ, multi, numerical, true/false, negative marks, skip = 0.
- APIs: preview, start, get, answer, complete, review.
- Server `must_submit_by`; auto-complete when expired.
- Flutter: home, chapters, setup, player, result, review.

## Tests

- `ScoringCalculatorTest`
- Answer-key leak regression on student DTO JSON

## How to test

```bash
cd infra
docker compose up -d
cd ../backend
./gradlew test
./gradlew bootRun
```

Register, choose NEET or JEE, open Home, start practice, submit, review.

## Known issues

- Player timer is a local countdown seeded from the server remaining time; official close uses `must_submit_by`.
- Mark-for-review is stored on the API but the player UI does not yet expose the toggle.
- No ads on the player.
