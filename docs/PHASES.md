# Development phases

Implement **one phase at a time**. After each phase: list files, Flyway migrations, APIs, tests, known issues, and test instructions — then wait for approval before the next phase.

Reports and playbooks live in `docs/phases/`.

| Phase | Name | Complexity | Status |
| --- | --- | --- | --- |
| 1 | Architecture | S | Done |
| 2 | Authentication | M | Done |
| 3 | Exam/content catalog | L | Done |
| 4 | Practice engine | L | Done |
| 5 | Mock exams | L | Playbook |
| 6 | Gamification | M | Playbook |
| 7 | Performance / study plan | M | Playbook |
| 8 | Leaderboard | M | Playbook |
| 9 | AI | L | Playbook |
| 10 | Monetization | L | Playbook |
| 11 | Admin panel | XL | Playbook |
| 12 | Testing hardening | L | Playbook |
| 13 | Beta | L | Playbook |
| 14 | Production | M | Playbook |

Complexity: S < 1 week, M 1–3 weeks, L 3–6 weeks, XL 6+ weeks (one API engineer + one Flutter engineer).

Private beta cut line: Phases 2–6 plus a thin admin. Commercial v1: through Phase 11.
