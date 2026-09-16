# Phase 5 playbook — Mock examinations

Depends on Phase 4 session/timer patterns.

## Goal

Database-driven exam configuration: duration, sections, negative marking, auto-submit, server result.

## Database

`mock_tests`, `mock_test_sections`, `mock_test_questions`, `mock_attempts`, `mock_attempt_answers`

## Rules

Do not hard-code JEE/NEET/NDA marking in Flutter. Read `exam_patterns` / `marking_schemes`.

## Tests

Auto-submit after `must_submit_by`, section navigation, negative marking, network interruption (resume from server remaining time).
