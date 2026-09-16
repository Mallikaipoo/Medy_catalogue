# Phase 3 playbook — Exam / content system

Implement only after Phase 2 is accepted.

## Goal

Exams, subjects, chapters, topics, questions, options, explanations. Students see only `PUBLISHED` content. Options never include `is_correct` on fetch.

## Database (Flyway V2+)

`exam_patterns`, `exam_pattern_sections`, `marking_schemes`, `subjects`, `exam_subjects`, `chapters`, `topics`, `question_types`, `languages`, `questions`, `question_options`, `question_explanations`, `explanation_steps`, `question_tags`, `question_tag_map`, `content_reviews`

Seed question types: SINGLE_MCQ, MULTI_MCQ, NUMERICAL, TRUE_FALSE.

## APIs

Student: `GET /exams/{id}/subjects`, chapters, topics, `GET /questions/{id}` without key.

Admin (can be thin): create/edit question as DRAFT.

## Tests

- Unpublished questions are hidden from student GET.
- Fetch question JSON has no `isCorrect`.
- Question type is data-driven.

## Flutter

Subject, chapter, topic lists. No practice player yet (Phase 4).
