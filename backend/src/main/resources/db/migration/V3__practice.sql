CREATE TABLE practice_sessions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    exam_id UUID NOT NULL REFERENCES exams (id),
    subject_id UUID REFERENCES subjects (id),
    chapter_id UUID REFERENCES chapters (id),
    topic_id UUID REFERENCES topics (id),
    difficulty VARCHAR(16),
    question_count INT NOT NULL,
    duration_seconds INT NOT NULL,
    start_at TIMESTAMPTZ NOT NULL,
    must_submit_by TIMESTAMPTZ NOT NULL,
    submitted_at TIMESTAMPTZ,
    status VARCHAR(32) NOT NULL,
    score NUMERIC(10, 2),
    total_marks NUMERIC(10, 2),
    correct_count INT,
    wrong_count INT,
    skipped_count INT,
    time_taken_seconds INT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_practice_sessions_user ON practice_sessions (user_id, created_at DESC);

CREATE TABLE practice_session_items (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    session_id UUID NOT NULL REFERENCES practice_sessions (id) ON DELETE CASCADE,
    question_id UUID NOT NULL REFERENCES questions (id),
    item_order INT NOT NULL,
    marked_for_review BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE UNIQUE INDEX idx_practice_items_session_order ON practice_session_items (session_id, item_order);
CREATE INDEX idx_practice_items_session ON practice_session_items (session_id);

CREATE TABLE practice_answers (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    session_id UUID NOT NULL REFERENCES practice_sessions (id) ON DELETE CASCADE,
    question_id UUID NOT NULL REFERENCES questions (id),
    selected_payload TEXT NOT NULL DEFAULT '{}',
    is_correct BOOLEAN,
    awarded_marks NUMERIC(8, 2),
    submitted_at TIMESTAMPTZ NOT NULL,
    UNIQUE (session_id, question_id)
);
