# Phase 4 playbook — Practice engine

Depends on Phase 3 published questions.

## Goal

Filter selection, start session, answer, server score, timer, results, review.

## Database

`practice_sessions`, `practice_session_items`, `practice_answers`

Store `start_at`, `must_submit_by`. Inject `Clock`. Do not trust device time for scoring.

## APIs

`POST /practice/preview|start`, `GET /practice/{id}`, `POST /practice/{id}/answer|complete`, `GET /practice/{id}/review` (keys only after complete).

## Tests

Scoring, negative marks, timeout reject, answer-key leak regression, mark-for-review.

## Flutter

Practice setup, question player (submit, previous/next, clear, mark), result, review. No ads on the player.
