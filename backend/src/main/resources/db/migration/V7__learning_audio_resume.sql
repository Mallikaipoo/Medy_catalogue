-- Free last-year gift, paid study notes, topic frequency, resume, and replayable doubts.

ALTER TABLE topics
    ADD COLUMN exam_hit_count INT NOT NULL DEFAULT 1,
    ADD COLUMN pattern_note TEXT,
    ADD COLUMN spoken_script TEXT;

ALTER TABLE questions
    ADD COLUMN is_free_preview BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN trap_wording TEXT,
    ADD COLUMN method_script TEXT;

CREATE TABLE question_variants (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    question_id UUID NOT NULL REFERENCES questions (id) ON DELETE CASCADE,
    variant_text TEXT NOT NULL,
    trap_note TEXT NOT NULL,
    sort_order INT NOT NULL DEFAULT 1
);

CREATE INDEX idx_question_variants_question ON question_variants (question_id);

CREATE TABLE user_resume_states (
    user_id UUID PRIMARY KEY REFERENCES users (id) ON DELETE CASCADE,
    exam_id UUID REFERENCES exams (id),
    route VARCHAR(96) NOT NULL,
    title VARCHAR(256) NOT NULL,
    payload TEXT NOT NULL DEFAULT '{}',
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE learning_queries (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    exam_id UUID REFERENCES exams (id),
    question_id UUID REFERENCES questions (id),
    topic_id UUID REFERENCES topics (id),
    source VARCHAR(16) NOT NULL,
    locale VARCHAR(16) NOT NULL,
    query_text TEXT NOT NULL,
    answer_short TEXT NOT NULL,
    spoken_script TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_learning_queries_user ON learning_queries (user_id, created_at DESC);

INSERT INTO plan_entitlements (id, plan_id, code, value_int, value_bool) VALUES
    ('c1000000-0000-0000-0000-000000000051', 'c1000000-0000-0000-0000-000000000001', 'full_question_bank', NULL, FALSE),
    ('c1000000-0000-0000-0000-000000000052', 'c1000000-0000-0000-0000-000000000001', 'study_material', NULL, FALSE),
    ('c1000000-0000-0000-0000-000000000053', 'c1000000-0000-0000-0000-000000000001', 'audio_explain', NULL, TRUE),
    ('c1000000-0000-0000-0000-000000000054', 'c1000000-0000-0000-0000-000000000001', 'doubt_tutor', NULL, FALSE),
    ('c1000000-0000-0000-0000-000000000055', 'c1000000-0000-0000-0000-000000000001', 'resume_library', NULL, FALSE),
    ('c1000000-0000-0000-0000-000000000061', 'c1000000-0000-0000-0000-000000000002', 'full_question_bank', NULL, TRUE),
    ('c1000000-0000-0000-0000-000000000062', 'c1000000-0000-0000-0000-000000000002', 'study_material', NULL, TRUE),
    ('c1000000-0000-0000-0000-000000000063', 'c1000000-0000-0000-0000-000000000002', 'audio_explain', NULL, TRUE),
    ('c1000000-0000-0000-0000-000000000064', 'c1000000-0000-0000-0000-000000000002', 'doubt_tutor', NULL, TRUE),
    ('c1000000-0000-0000-0000-000000000065', 'c1000000-0000-0000-0000-000000000002', 'resume_library', NULL, TRUE);

-- One last-year (2025-style) gift question per exam, with answer and explanation.
UPDATE questions SET
    is_free_preview = TRUE,
    trap_wording = 'The same idea is often rewritten with extra words so students pick a look-alike option.',
    method_script = 'Read the physical quantity being asked. Strike options that mix units or similar-sounding laws. State the definition, then pick.'
WHERE id IN (
    'a4100000-0000-4000-8000-000000000010',
    'a4100000-0000-4000-8000-000000000070',
    'a4100000-0000-4000-8000-000000000080'
);

INSERT INTO question_variants (id, question_id, variant_text, trap_note, sort_order) VALUES
    ('a5100000-0000-4000-8000-000000000001', 'a4100000-0000-4000-8000-000000000010',
     'Two charges of the same sign placed a small distance apart will:',
     'Same meaning as “like charges repel”, but the sentence hides the word “like”.', 1),
    ('a5100000-0000-4000-8000-000000000002', 'a4100000-0000-4000-8000-000000000010',
     'If q1 and q2 are both positive, the Coulomb force on each is:',
     'Positive-positive is still like charges. Do not confuse with unlike attract.', 2),
    ('a5100000-0000-4000-8000-000000000003', 'a4100000-0000-4000-8000-000000000070',
     'In humans, fusion of gametes typically takes place in which part of the oviduct?',
     '“Oviduct” and “fallopian tube” are the same region. Ampulla is the usual site.', 1),
    ('a5100000-0000-4000-8000-000000000004', 'a4100000-0000-4000-8000-000000000070',
     'Where do sperm and ovum normally meet after ovulation?',
     'Meeting place is not the uterus. Uterus is implantation, not fertilisation.', 2),
    ('a5100000-0000-4000-8000-000000000005', 'a4100000-0000-4000-8000-000000000080',
     'cos²θ is identical to which of the following?',
     '1 − sin²θ is the identity. Options may show 1 + sin²θ to catch sign errors.', 1),
    ('a5100000-0000-4000-8000-000000000006', 'a4100000-0000-4000-8000-000000000080',
     'Using the Pythagorean identity, 1 minus sine squared of theta equals:',
     'Same question, longer English. The algebra does not change.', 2);

UPDATE topics t
SET exam_hit_count = GREATEST(1, LEAST(2, sub.cnt)),
    pattern_note = 'Papers often ask this same idea in a different sentence to confuse you. Slow down, name the quantity, then choose.',
    spoken_script = left(coalesce(t.detailed_explanation, t.key_points, t.name), 900)
FROM (
    SELECT topic_id, COUNT(*)::int AS cnt
    FROM questions
    WHERE status = 'PUBLISHED'
    GROUP BY topic_id
) sub
WHERE t.id = sub.topic_id;
