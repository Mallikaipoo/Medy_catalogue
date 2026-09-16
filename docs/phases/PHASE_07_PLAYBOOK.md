# Phase 7 playbook — Performance and study plan

## Goal

Aggregates, weak topics, recommendations, explainable study plan.

## Database

`user_question_attempts`, `user_topic_stats`, `user_chapter_stats`, `user_subject_stats`, `user_daily_activity`, `study_plans`, `study_plan_items`

## APIs

`GET /performance`, `GET /performance/weak-topics`, `GET/PUT /study-plan`

## Tests

Weak-topic ranking, plan generation given hours/exam date (deterministic unit tests).
