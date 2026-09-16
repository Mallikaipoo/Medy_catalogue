# Phase 6 playbook — Gamification

## Goal

XP ledger, levels, streaks (server date), badges, achievements, daily challenge. Not pay-to-win.

## Database

`xp_ledger`, `user_gamification`, `achievements`, `user_achievements`, `badges`, `user_badges`, `goals`, `user_goal_progress`, `daily_challenges`, `daily_challenge_attempts`

Unique `(user_id, challenge_id)` so rewards cannot be claimed twice. Validate completion on the server.

## Tests

Streak increment/reset using injected Clock, XP awards, duplicate daily reward rejected.
