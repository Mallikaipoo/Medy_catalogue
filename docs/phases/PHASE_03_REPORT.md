# Phase 3 report — Exam / content system

- Date: 2026-09-16
- Goal: Exams, subjects, chapters, topics, published questions without leaking keys.

## Completed

- Flyway `V2__catalog.sql` with types, hierarchy, explanations, seed JEE/NEET/NDA content.
- Student APIs: home, subjects, chapters, topics, published question fetch.
- Draft seed question is not returned to students.

## Files

- `backend/src/main/resources/db/migration/V2__catalog.sql`
- `backend/src/main/java/in/techgeneza/medycatalog/modules/catalog/**`

## Tests

- `StudentQuestionMapperTest` (no `isCorrect` in student JSON)
- `CatalogControllerTest` (unpublished → 404)

## Known issues

- Seed bank is small (demo content). Import tools are Phase 11.
- Home progress % is question-bank size, not personal accuracy (Phase 7).
