-- Catalog: exam-agnostic hierarchy and published-only student content.

CREATE TABLE question_types (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code VARCHAR(32) NOT NULL UNIQUE,
    name VARCHAR(80) NOT NULL,
    allows_multiple_options BOOLEAN NOT NULL DEFAULT FALSE,
    requires_numerical BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE languages (
    code VARCHAR(16) PRIMARY KEY,
    name VARCHAR(80) NOT NULL
);

CREATE TABLE exam_patterns (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    exam_id UUID NOT NULL REFERENCES exams (id),
    name VARCHAR(120) NOT NULL,
    duration_seconds INT NOT NULL,
    section_navigation BOOLEAN NOT NULL DEFAULT TRUE,
    auto_submit BOOLEAN NOT NULL DEFAULT TRUE,
    config_json TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE exam_pattern_sections (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    pattern_id UUID NOT NULL REFERENCES exam_patterns (id) ON DELETE CASCADE,
    name VARCHAR(120) NOT NULL,
    sort_order INT NOT NULL DEFAULT 0,
    question_count INT,
    time_seconds INT
);

CREATE TABLE marking_schemes (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    exam_id UUID NOT NULL REFERENCES exams (id),
    question_type_id UUID NOT NULL REFERENCES question_types (id),
    correct_marks NUMERIC(8, 2) NOT NULL,
    negative_marks NUMERIC(8, 2) NOT NULL DEFAULT 0,
    unanswered_marks NUMERIC(8, 2) NOT NULL DEFAULT 0,
    UNIQUE (exam_id, question_type_id)
);

CREATE TABLE subjects (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code VARCHAR(32) NOT NULL UNIQUE,
    name VARCHAR(120) NOT NULL
);

CREATE TABLE exam_subjects (
    exam_id UUID NOT NULL REFERENCES exams (id),
    subject_id UUID NOT NULL REFERENCES subjects (id),
    sort_order INT NOT NULL DEFAULT 0,
    PRIMARY KEY (exam_id, subject_id)
);

CREATE TABLE chapters (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    exam_id UUID NOT NULL REFERENCES exams (id),
    subject_id UUID NOT NULL REFERENCES subjects (id),
    name VARCHAR(200) NOT NULL,
    sort_order INT NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE topics (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    chapter_id UUID NOT NULL REFERENCES chapters (id) ON DELETE CASCADE,
    name VARCHAR(200) NOT NULL,
    sort_order INT NOT NULL DEFAULT 0
);

CREATE TABLE questions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    exam_id UUID NOT NULL REFERENCES exams (id),
    subject_id UUID NOT NULL REFERENCES subjects (id),
    chapter_id UUID NOT NULL REFERENCES chapters (id),
    topic_id UUID NOT NULL REFERENCES topics (id),
    question_type_id UUID NOT NULL REFERENCES question_types (id),
    question_text TEXT NOT NULL,
    difficulty VARCHAR(16) NOT NULL,
    marks NUMERIC(8, 2) NOT NULL,
    negative_marks NUMERIC(8, 2) NOT NULL DEFAULT 0,
    estimated_time_seconds INT NOT NULL DEFAULT 60,
    exam_year INT,
    source_type VARCHAR(32),
    source_reference VARCHAR(255),
    language VARCHAR(16) NOT NULL DEFAULT 'en',
    status VARCHAR(32) NOT NULL,
    numerical_answer TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_questions_catalog_status
    ON questions (exam_id, subject_id, chapter_id, topic_id, status, difficulty);

CREATE TABLE question_options (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    question_id UUID NOT NULL REFERENCES questions (id) ON DELETE CASCADE,
    option_text TEXT NOT NULL,
    option_order INT NOT NULL,
    is_correct BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_question_options_question ON question_options (question_id);

CREATE TABLE question_explanations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    question_id UUID NOT NULL UNIQUE REFERENCES questions (id) ON DELETE CASCADE,
    simple_text TEXT,
    detailed_text TEXT,
    why_others_wrong TEXT,
    related_concept_topic_id UUID REFERENCES topics (id),
    exam_tip TEXT
);

CREATE TABLE explanation_steps (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    question_id UUID NOT NULL REFERENCES questions (id) ON DELETE CASCADE,
    step_order INT NOT NULL,
    title VARCHAR(120),
    body TEXT NOT NULL
);

CREATE TABLE question_tags (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(64) NOT NULL UNIQUE
);

CREATE TABLE question_tag_map (
    question_id UUID NOT NULL REFERENCES questions (id) ON DELETE CASCADE,
    tag_id UUID NOT NULL REFERENCES question_tags (id) ON DELETE CASCADE,
    PRIMARY KEY (question_id, tag_id)
);

CREATE TABLE content_reviews (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    question_id UUID NOT NULL REFERENCES questions (id) ON DELETE CASCADE,
    reviewer_id UUID REFERENCES users (id),
    from_status VARCHAR(32),
    to_status VARCHAR(32) NOT NULL,
    comment TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

INSERT INTO languages (code, name) VALUES ('en', 'English'), ('hi', 'Hindi');

INSERT INTO question_types (id, code, name, allows_multiple_options, requires_numerical) VALUES
    ('d0000000-0000-0000-0000-000000000001', 'SINGLE_MCQ', 'Single-choice MCQ', FALSE, FALSE),
    ('d0000000-0000-0000-0000-000000000002', 'MULTI_MCQ', 'Multiple-choice', TRUE, FALSE),
    ('d0000000-0000-0000-0000-000000000003', 'NUMERICAL', 'Numerical answer', FALSE, TRUE),
    ('d0000000-0000-0000-0000-000000000004', 'TRUE_FALSE', 'True / False', FALSE, FALSE);

INSERT INTO subjects (id, code, name) VALUES
    ('e1000000-0000-0000-0000-000000000001', 'PHY', 'Physics'),
    ('e1000000-0000-0000-0000-000000000002', 'CHEM', 'Chemistry'),
    ('e1000000-0000-0000-0000-000000000003', 'MATH', 'Mathematics'),
    ('e1000000-0000-0000-0000-000000000004', 'BIO', 'Biology'),
    ('e1000000-0000-0000-0000-000000000005', 'GAT', 'General Ability Test');

-- JEE
INSERT INTO exam_subjects (exam_id, subject_id, sort_order) VALUES
    ('c0000000-0000-0000-0000-000000000001', 'e1000000-0000-0000-0000-000000000001', 1),
    ('c0000000-0000-0000-0000-000000000001', 'e1000000-0000-0000-0000-000000000002', 2),
    ('c0000000-0000-0000-0000-000000000001', 'e1000000-0000-0000-0000-000000000003', 3);
-- NEET
INSERT INTO exam_subjects (exam_id, subject_id, sort_order) VALUES
    ('c0000000-0000-0000-0000-000000000002', 'e1000000-0000-0000-0000-000000000001', 1),
    ('c0000000-0000-0000-0000-000000000002', 'e1000000-0000-0000-0000-000000000002', 2),
    ('c0000000-0000-0000-0000-000000000002', 'e1000000-0000-0000-0000-000000000004', 3);
-- NDA
INSERT INTO exam_subjects (exam_id, subject_id, sort_order) VALUES
    ('c0000000-0000-0000-0000-000000000003', 'e1000000-0000-0000-0000-000000000003', 1),
    ('c0000000-0000-0000-0000-000000000003', 'e1000000-0000-0000-0000-000000000005', 2);

INSERT INTO marking_schemes (id, exam_id, question_type_id, correct_marks, negative_marks, unanswered_marks) VALUES
    ('d1000000-0000-0000-0000-000000000001', 'c0000000-0000-0000-0000-000000000001', 'd0000000-0000-0000-0000-000000000001', 4, 1, 0),
    ('d1000000-0000-0000-0000-000000000002', 'c0000000-0000-0000-0000-000000000002', 'd0000000-0000-0000-0000-000000000001', 4, 1, 0),
    ('d1000000-0000-0000-0000-000000000003', 'c0000000-0000-0000-0000-000000000003', 'd0000000-0000-0000-0000-000000000001', 4, 1.33, 0);

INSERT INTO exam_patterns (id, exam_id, name, duration_seconds, section_navigation, auto_submit) VALUES
    ('d2000000-0000-0000-0000-000000000001', 'c0000000-0000-0000-0000-000000000001', 'JEE Main default', 10800, TRUE, TRUE),
    ('d2000000-0000-0000-0000-000000000002', 'c0000000-0000-0000-0000-000000000002', 'NEET default', 12000, TRUE, TRUE),
    ('d2000000-0000-0000-0000-000000000003', 'c0000000-0000-0000-0000-000000000003', 'NDA Mathematics', 9000, TRUE, TRUE);

INSERT INTO chapters (id, exam_id, subject_id, name, sort_order) VALUES
    ('e2000000-0000-0000-0000-000000000001', 'c0000000-0000-0000-0000-000000000001', 'e1000000-0000-0000-0000-000000000001', 'Laws of Motion', 1),
    ('e2000000-0000-0000-0000-000000000002', 'c0000000-0000-0000-0000-000000000001', 'e1000000-0000-0000-0000-000000000002', 'Atomic Structure', 1),
    ('e2000000-0000-0000-0000-000000000003', 'c0000000-0000-0000-0000-000000000001', 'e1000000-0000-0000-0000-000000000003', 'Quadratic Equations', 1),
    ('e2000000-0000-0000-0000-000000000004', 'c0000000-0000-0000-0000-000000000002', 'e1000000-0000-0000-0000-000000000001', 'Kinematics', 1),
    ('e2000000-0000-0000-0000-000000000005', 'c0000000-0000-0000-0000-000000000002', 'e1000000-0000-0000-0000-000000000002', 'Chemical Bonding', 1),
    ('e2000000-0000-0000-0000-000000000006', 'c0000000-0000-0000-0000-000000000002', 'e1000000-0000-0000-0000-000000000004', 'Cell Structure', 1),
    ('e2000000-0000-0000-0000-000000000007', 'c0000000-0000-0000-0000-000000000003', 'e1000000-0000-0000-0000-000000000003', 'Algebra', 1),
    ('e2000000-0000-0000-0000-000000000008', 'c0000000-0000-0000-0000-000000000003', 'e1000000-0000-0000-0000-000000000005', 'General Science', 1);

INSERT INTO topics (id, chapter_id, name, sort_order) VALUES
    ('e3000000-0000-0000-0000-000000000001', 'e2000000-0000-0000-0000-000000000001', 'Newton''s laws', 1),
    ('e3000000-0000-0000-0000-000000000002', 'e2000000-0000-0000-0000-000000000002', 'Bohr model', 1),
    ('e3000000-0000-0000-0000-000000000003', 'e2000000-0000-0000-0000-000000000003', 'Nature of roots', 1),
    ('e3000000-0000-0000-0000-000000000004', 'e2000000-0000-0000-0000-000000000004', 'Equations of motion', 1),
    ('e3000000-0000-0000-0000-000000000005', 'e2000000-0000-0000-0000-000000000005', 'Ionic bonding', 1),
    ('e3000000-0000-0000-0000-000000000006', 'e2000000-0000-0000-0000-000000000006', 'Mitochondria', 1),
    ('e3000000-0000-0000-0000-000000000007', 'e2000000-0000-0000-0000-000000000007', 'Linear equations', 1),
    ('e3000000-0000-0000-0000-000000000008', 'e2000000-0000-0000-0000-000000000008', 'Everyday physics', 1);

INSERT INTO questions (
    id, exam_id, subject_id, chapter_id, topic_id, question_type_id, question_text,
    difficulty, marks, negative_marks, estimated_time_seconds, language, status, numerical_answer, source_type
) VALUES
    ('e4000000-0000-0000-0000-000000000001', 'c0000000-0000-0000-0000-000000000001', 'e1000000-0000-0000-0000-000000000001', 'e2000000-0000-0000-0000-000000000001', 'e3000000-0000-0000-0000-000000000001', 'd0000000-0000-0000-0000-000000000001',
     'The SI unit of force is:', 'EASY', 4, 1, 45, 'en', 'PUBLISHED', NULL, 'SEED'),
    ('e4000000-0000-0000-0000-000000000002', 'c0000000-0000-0000-0000-000000000001', 'e1000000-0000-0000-0000-000000000001', 'e2000000-0000-0000-0000-000000000001', 'e3000000-0000-0000-0000-000000000001', 'd0000000-0000-0000-0000-000000000004',
     'An object at rest remains at rest unless acted upon by a net external force.', 'EASY', 4, 1, 30, 'en', 'PUBLISHED', NULL, 'SEED'),
    ('e4000000-0000-0000-0000-000000000003', 'c0000000-0000-0000-0000-000000000001', 'e1000000-0000-0000-0000-000000000002', 'e2000000-0000-0000-0000-000000000002', 'e3000000-0000-0000-0000-000000000002', 'd0000000-0000-0000-0000-000000000003',
     'The atomic number of carbon is:', 'EASY', 4, 1, 40, 'en', 'PUBLISHED', '6', 'SEED'),
    ('e4000000-0000-0000-0000-000000000004', 'c0000000-0000-0000-0000-000000000001', 'e1000000-0000-0000-0000-000000000003', 'e2000000-0000-0000-0000-000000000003', 'e3000000-0000-0000-0000-000000000003', 'd0000000-0000-0000-0000-000000000002',
     'Which of the following are roots of x² − 5x + 6 = 0? Select all that apply.', 'MEDIUM', 4, 1, 60, 'en', 'PUBLISHED', NULL, 'SEED'),
    ('e4000000-0000-0000-0000-000000000005', 'c0000000-0000-0000-0000-000000000002', 'e1000000-0000-0000-0000-000000000004', 'e2000000-0000-0000-0000-000000000006', 'e3000000-0000-0000-0000-000000000006', 'd0000000-0000-0000-0000-000000000001',
     'Which organelle is the primary site of ATP synthesis in a typical eukaryotic cell?', 'EASY', 4, 1, 45, 'en', 'PUBLISHED', NULL, 'SEED'),
    ('e4000000-0000-0000-0000-000000000006', 'c0000000-0000-0000-0000-000000000002', 'e1000000-0000-0000-0000-000000000004', 'e2000000-0000-0000-0000-000000000006', 'e3000000-0000-0000-0000-000000000006', 'd0000000-0000-0000-0000-000000000001',
     'Mitochondria are bounded by how many membranes?', 'MEDIUM', 4, 1, 45, 'en', 'PUBLISHED', NULL, 'SEED'),
    ('e4000000-0000-0000-0000-000000000007', 'c0000000-0000-0000-0000-000000000002', 'e1000000-0000-0000-0000-000000000001', 'e2000000-0000-0000-0000-000000000004', 'e3000000-0000-0000-0000-000000000004', 'd0000000-0000-0000-0000-000000000001',
     'If a body starts from rest and acceleration is constant, displacement after time t is:', 'MEDIUM', 4, 1, 60, 'en', 'PUBLISHED', NULL, 'SEED'),
    ('e4000000-0000-0000-0000-000000000008', 'c0000000-0000-0000-0000-000000000002', 'e1000000-0000-0000-0000-000000000002', 'e2000000-0000-0000-0000-000000000005', 'e3000000-0000-0000-0000-000000000005', 'd0000000-0000-0000-0000-000000000001',
     'An ionic bond is typically formed by:', 'EASY', 4, 1, 40, 'en', 'PUBLISHED', NULL, 'SEED'),
    ('e4000000-0000-0000-0000-000000000009', 'c0000000-0000-0000-0000-000000000003', 'e1000000-0000-0000-0000-000000000003', 'e2000000-0000-0000-0000-000000000007', 'e3000000-0000-0000-0000-000000000007', 'd0000000-0000-0000-0000-000000000001',
     'The solution of 2x + 6 = 0 is:', 'EASY', 4, 1.33, 40, 'en', 'PUBLISHED', NULL, 'SEED'),
    ('e4000000-0000-0000-0000-00000000000a', 'c0000000-0000-0000-0000-000000000003', 'e1000000-0000-0000-0000-000000000005', 'e2000000-0000-0000-0000-000000000008', 'e3000000-0000-0000-0000-000000000008', 'd0000000-0000-0000-0000-000000000004',
     'Light travels faster in vacuum than sound does in air.', 'EASY', 4, 1.33, 30, 'en', 'PUBLISHED', NULL, 'SEED'),
    ('e4000000-0000-0000-0000-00000000000b', 'c0000000-0000-0000-0000-000000000001', 'e1000000-0000-0000-0000-000000000001', 'e2000000-0000-0000-0000-000000000001', 'e3000000-0000-0000-0000-000000000001', 'd0000000-0000-0000-0000-000000000001',
     'Newton''s second law is best expressed as:', 'MEDIUM', 4, 1, 50, 'en', 'PUBLISHED', NULL, 'SEED'),
    ('e4000000-0000-0000-0000-000000000099', 'c0000000-0000-0000-0000-000000000002', 'e1000000-0000-0000-0000-000000000004', 'e2000000-0000-0000-0000-000000000006', 'e3000000-0000-0000-0000-000000000006', 'd0000000-0000-0000-0000-000000000001',
     'DRAFT ONLY — this unpublished item must never reach students.', 'EASY', 4, 1, 30, 'en', 'DRAFT', NULL, 'SEED');

INSERT INTO question_options (id, question_id, option_text, option_order, is_correct) VALUES
    ('e5000000-0000-0000-0000-000000000001', 'e4000000-0000-0000-0000-000000000001', 'Joule', 1, FALSE),
    ('e5000000-0000-0000-0000-000000000002', 'e4000000-0000-0000-0000-000000000001', 'Newton', 2, TRUE),
    ('e5000000-0000-0000-0000-000000000003', 'e4000000-0000-0000-0000-000000000001', 'Watt', 3, FALSE),
    ('e5000000-0000-0000-0000-000000000004', 'e4000000-0000-0000-0000-000000000001', 'Pascal', 4, FALSE),
    ('e5000000-0000-0000-0000-000000000005', 'e4000000-0000-0000-0000-000000000002', 'True', 1, TRUE),
    ('e5000000-0000-0000-0000-000000000006', 'e4000000-0000-0000-0000-000000000002', 'False', 2, FALSE),
    ('e5000000-0000-0000-0000-000000000007', 'e4000000-0000-0000-0000-000000000004', '1', 1, FALSE),
    ('e5000000-0000-0000-0000-000000000008', 'e4000000-0000-0000-0000-000000000004', '2', 2, TRUE),
    ('e5000000-0000-0000-0000-000000000009', 'e4000000-0000-0000-0000-000000000004', '3', 3, TRUE),
    ('e5000000-0000-0000-0000-00000000000a', 'e4000000-0000-0000-0000-000000000004', '6', 4, FALSE),
    ('e5000000-0000-0000-0000-00000000000b', 'e4000000-0000-0000-0000-000000000005', 'Ribosome', 1, FALSE),
    ('e5000000-0000-0000-0000-00000000000c', 'e4000000-0000-0000-0000-000000000005', 'Mitochondrion', 2, TRUE),
    ('e5000000-0000-0000-0000-00000000000d', 'e4000000-0000-0000-0000-000000000005', 'Golgi apparatus', 3, FALSE),
    ('e5000000-0000-0000-0000-00000000000e', 'e4000000-0000-0000-0000-000000000005', 'Lysosome', 4, FALSE),
    ('e5000000-0000-0000-0000-00000000000f', 'e4000000-0000-0000-0000-000000000006', 'One', 1, FALSE),
    ('e5000000-0000-0000-0000-000000000010', 'e4000000-0000-0000-0000-000000000006', 'Two', 2, TRUE),
    ('e5000000-0000-0000-0000-000000000011', 'e4000000-0000-0000-0000-000000000006', 'Three', 3, FALSE),
    ('e5000000-0000-0000-0000-000000000012', 'e4000000-0000-0000-0000-000000000006', 'None', 4, FALSE),
    ('e5000000-0000-0000-0000-000000000013', 'e4000000-0000-0000-0000-000000000007', 'vt', 1, FALSE),
    ('e5000000-0000-0000-0000-000000000014', 'e4000000-0000-0000-0000-000000000007', 'ut + ½at²', 2, TRUE),
    ('e5000000-0000-0000-0000-000000000015', 'e4000000-0000-0000-0000-000000000007', 'at', 3, FALSE),
    ('e5000000-0000-0000-0000-000000000016', 'e4000000-0000-0000-0000-000000000007', '½mv²', 4, FALSE),
    ('e5000000-0000-0000-0000-000000000017', 'e4000000-0000-0000-0000-000000000008', 'Sharing of electrons between two atoms', 1, FALSE),
    ('e5000000-0000-0000-0000-000000000018', 'e4000000-0000-0000-0000-000000000008', 'Transfer of electrons from a metal to a non-metal', 2, TRUE),
    ('e5000000-0000-0000-0000-000000000019', 'e4000000-0000-0000-0000-000000000008', 'Hydrogen bonding only', 3, FALSE),
    ('e5000000-0000-0000-0000-00000000001a', 'e4000000-0000-0000-0000-000000000008', 'Metallic lattices only', 4, FALSE),
    ('e5000000-0000-0000-0000-00000000001b', 'e4000000-0000-0000-0000-000000000009', 'x = 3', 1, FALSE),
    ('e5000000-0000-0000-0000-00000000001c', 'e4000000-0000-0000-0000-000000000009', 'x = −3', 2, TRUE),
    ('e5000000-0000-0000-0000-00000000001d', 'e4000000-0000-0000-0000-000000000009', 'x = 6', 3, FALSE),
    ('e5000000-0000-0000-0000-00000000001e', 'e4000000-0000-0000-0000-000000000009', 'x = −6', 4, FALSE),
    ('e5000000-0000-0000-0000-00000000001f', 'e4000000-0000-0000-0000-00000000000a', 'True', 1, TRUE),
    ('e5000000-0000-0000-0000-000000000020', 'e4000000-0000-0000-0000-00000000000a', 'False', 2, FALSE),
    ('e5000000-0000-0000-0000-000000000021', 'e4000000-0000-0000-0000-00000000000b', 'F = ma', 1, TRUE),
    ('e5000000-0000-0000-0000-000000000022', 'e4000000-0000-0000-0000-00000000000b', 'F = mv', 2, FALSE),
    ('e5000000-0000-0000-0000-000000000023', 'e4000000-0000-0000-0000-00000000000b', 'F = m/a', 3, FALSE),
    ('e5000000-0000-0000-0000-000000000024', 'e4000000-0000-0000-0000-00000000000b', 'F = a/m', 4, FALSE),
    ('e5000000-0000-0000-0000-000000000099', 'e4000000-0000-0000-0000-000000000099', 'Hidden A', 1, TRUE),
    ('e5000000-0000-0000-0000-00000000009a', 'e4000000-0000-0000-0000-000000000099', 'Hidden B', 2, FALSE);

INSERT INTO question_explanations (id, question_id, simple_text, detailed_text, why_others_wrong, related_concept_topic_id, exam_tip) VALUES
    ('e6000000-0000-0000-0000-000000000001', 'e4000000-0000-0000-0000-000000000001', 'Force is measured in newtons.', 'By definition 1 N = 1 kg·m/s². Joule is energy, watt is power, pascal is pressure.', 'Joule, watt and pascal measure other quantities.', 'e3000000-0000-0000-0000-000000000001', 'Memorise SI base combinations for mechanics.'),
    ('e6000000-0000-0000-0000-000000000002', 'e4000000-0000-0000-0000-000000000002', 'This is Newton''s first law.', 'Net force zero implies constant velocity, including zero velocity.', NULL, 'e3000000-0000-0000-0000-000000000001', 'Rest is a special case of uniform velocity.'),
    ('e6000000-0000-0000-0000-000000000003', 'e4000000-0000-0000-0000-000000000003', 'Carbon has 6 protons, so Z = 6.', 'Atomic number equals proton count. Carbon-12 also has 6 neutrons, but that is mass number 12.', NULL, 'e3000000-0000-0000-0000-000000000002', 'Do not confuse atomic number with mass number.'),
    ('e6000000-0000-0000-0000-000000000004', 'e4000000-0000-0000-0000-000000000004', 'Factor as (x−2)(x−3)=0.', 'Product of roots is 6 and sum is 5, so 2 and 3.', '1 and 6 do not satisfy the equation.', 'e3000000-0000-0000-0000-000000000003', 'Check by substitution when options are small integers.'),
    ('e6000000-0000-0000-0000-000000000005', 'e4000000-0000-0000-0000-000000000005', 'Mitochondria produce most ATP via oxidative phosphorylation.', 'The inner membrane holds the electron transport chain. Ribosomes make protein; Golgi packages; lysosomes digest.', 'Other listed organelles have different primary jobs.', 'e3000000-0000-0000-0000-000000000006', 'Link structure (cristae) to function (ATP).'),
    ('e6000000-0000-0000-0000-000000000006', 'e4000000-0000-0000-0000-000000000006', 'Outer and inner membranes — a double membrane.', 'The intermembrane space and matrix are separated by the inner membrane.', NULL, 'e3000000-0000-0000-0000-000000000006', 'Double membrane is a hallmark of mitochondria and chloroplasts.'),
    ('e6000000-0000-0000-0000-000000000007', 'e4000000-0000-0000-0000-000000000007', 'From rest, u = 0 so s = ½at², which is the u=0 case of ut + ½at².', 'v = u + at and v² = u² + 2as are the other two equations.', 'vt assumes constant velocity; ½mv² is kinetic energy.', 'e3000000-0000-0000-0000-000000000004', 'Write knowns (u, a, t) before picking a formula.'),
    ('e6000000-0000-0000-0000-000000000008', 'e4000000-0000-0000-0000-000000000008', 'Metals lose electrons, non-metals gain them.', 'Large electronegativity difference favours ion formation rather than sharing.', 'Covalent bonding is sharing; hydrogen bonds are intermolecular.', 'e3000000-0000-0000-0000-000000000005', 'Compare electronegativity before naming the bond type.'),
    ('e6000000-0000-0000-0000-000000000009', 'e4000000-0000-0000-0000-000000000009', '2x = −6 so x = −3.', 'Divide both sides by 2 after moving the constant.', NULL, 'e3000000-0000-0000-0000-000000000007', 'Transpose constants first, then divide.'),
    ('e6000000-0000-0000-0000-00000000000a', 'e4000000-0000-0000-0000-00000000000a', 'c ≈ 3×10⁸ m/s in vacuum; sound in air is about 340 m/s.', 'Sound needs a medium; light does not.', NULL, 'e3000000-0000-0000-0000-000000000008', 'Order-of-magnitude checks catch true/false traps.'),
    ('e6000000-0000-0000-0000-00000000000b', 'e4000000-0000-0000-0000-00000000000b', 'Net force equals mass times acceleration.', 'Momentum p = mv, so F = dp/dt reduces to ma for constant mass.', 'F = mv has wrong dimensions.', 'e3000000-0000-0000-0000-000000000001', 'Always check dimensions of mechanical formulae.');

INSERT INTO explanation_steps (id, question_id, step_order, title, body) VALUES
    ('e7000000-0000-0000-0000-000000000001', 'e4000000-0000-0000-0000-000000000003', 1, 'Definition', 'Atomic number Z = number of protons.'),
    ('e7000000-0000-0000-0000-000000000002', 'e4000000-0000-0000-0000-000000000003', 2, 'Recall', 'Carbon is element 6 in the periodic table.'),
    ('e7000000-0000-0000-0000-000000000003', 'e4000000-0000-0000-0000-000000000003', 3, 'Final answer', 'Z = 6'),
    ('e7000000-0000-0000-0000-000000000004', 'e4000000-0000-0000-0000-000000000007', 1, 'Formula', 's = ut + ½at²'),
    ('e7000000-0000-0000-0000-000000000005', 'e4000000-0000-0000-0000-000000000007', 2, 'Substitution', 'u = 0 for a start from rest.'),
    ('e7000000-0000-0000-0000-000000000006', 'e4000000-0000-0000-0000-000000000007', 3, 'Final answer', 's = ½at², which matches option ut + ½at² with u = 0.');

INSERT INTO content_reviews (id, question_id, from_status, to_status, comment) VALUES
    ('e8000000-0000-0000-0000-000000000001', 'e4000000-0000-0000-0000-000000000001', 'DRAFT', 'PUBLISHED', 'Seed review');
