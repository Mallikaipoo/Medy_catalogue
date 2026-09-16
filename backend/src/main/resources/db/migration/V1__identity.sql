-- MedyCatalog V1: identity, RBAC, exam picker seed
-- Flyway is the only schema path.

CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(120) NOT NULL,
    email VARCHAR(255),
    phone VARCHAR(32),
    profile_photo_url TEXT,
    date_of_birth DATE,
    country VARCHAR(64),
    state VARCHAR(64),
    city VARCHAR(64),
    preferred_language VARCHAR(16) NOT NULL DEFAULT 'en',
    account_status VARCHAR(32) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    last_login_at TIMESTAMPTZ,
    deleted_at TIMESTAMPTZ,
    CONSTRAINT users_email_unique UNIQUE (email)
);

CREATE TABLE user_identities (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    provider VARCHAR(32) NOT NULL,
    provider_subject VARCHAR(255) NOT NULL,
    password_hash TEXT,
    verified_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT user_identities_provider_subject UNIQUE (provider, provider_subject)
);

CREATE INDEX idx_user_identities_user_id ON user_identities (user_id);

CREATE TABLE refresh_tokens (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    token_hash VARCHAR(64) NOT NULL UNIQUE,
    device_name VARCHAR(120),
    expires_at TIMESTAMPTZ NOT NULL,
    revoked_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_refresh_tokens_user_id ON refresh_tokens (user_id);

CREATE TABLE otp_challenges (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    destination VARCHAR(255) NOT NULL,
    purpose VARCHAR(32) NOT NULL,
    code_hash VARCHAR(64) NOT NULL,
    expires_at TIMESTAMPTZ NOT NULL,
    attempts INT NOT NULL DEFAULT 0,
    max_attempts INT NOT NULL DEFAULT 5,
    consumed_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_otp_challenges_destination ON otp_challenges (destination, purpose);

CREATE TABLE roles (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code VARCHAR(64) NOT NULL UNIQUE,
    description TEXT
);

CREATE TABLE permissions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code VARCHAR(64) NOT NULL UNIQUE,
    description TEXT
);

CREATE TABLE role_permissions (
    role_id UUID NOT NULL REFERENCES roles (id) ON DELETE CASCADE,
    permission_id UUID NOT NULL REFERENCES permissions (id) ON DELETE CASCADE,
    PRIMARY KEY (role_id, permission_id)
);

CREATE TABLE user_roles (
    user_id UUID NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    role_id UUID NOT NULL REFERENCES roles (id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, role_id)
);

CREATE TABLE user_privacy_settings (
    user_id UUID PRIMARY KEY REFERENCES users (id) ON DELETE CASCADE,
    display_name VARCHAR(120),
    leaderboard_visibility VARCHAR(32) NOT NULL DEFAULT 'GLOBAL',
    analytics_consent BOOLEAN NOT NULL DEFAULT FALSE,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE user_notification_preferences (
    user_id UUID PRIMARY KEY REFERENCES users (id) ON DELETE CASCADE,
    daily_challenge BOOLEAN NOT NULL DEFAULT TRUE,
    streak_reminders BOOLEAN NOT NULL DEFAULT TRUE,
    study_reminders BOOLEAN NOT NULL DEFAULT TRUE,
    marketing BOOLEAN NOT NULL DEFAULT FALSE,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE parental_consents (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    guardian_email VARCHAR(255),
    status VARCHAR(32) NOT NULL DEFAULT 'PENDING',
    consented_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE account_deletion_requests (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    requested_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    purge_after TIMESTAMPTZ NOT NULL,
    status VARCHAR(32) NOT NULL DEFAULT 'PENDING'
);

CREATE TABLE exams (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code VARCHAR(32) NOT NULL UNIQUE,
    name VARCHAR(120) NOT NULL,
    status VARCHAR(32) NOT NULL DEFAULT 'ACTIVE',
    default_language VARCHAR(16) NOT NULL DEFAULT 'en',
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE user_exam_enrollments (
    user_id UUID NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    exam_id UUID NOT NULL REFERENCES exams (id),
    is_primary BOOLEAN NOT NULL DEFAULT FALSE,
    enrolled_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    PRIMARY KEY (user_id, exam_id)
);

CREATE TABLE audit_logs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    actor_user_id UUID,
    action VARCHAR(64) NOT NULL,
    entity_type VARCHAR(64),
    entity_id VARCHAR(64),
    ip_address VARCHAR(64),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_audit_logs_created_at ON audit_logs (created_at);

INSERT INTO roles (id, code, description) VALUES
    ('a0000000-0000-0000-0000-000000000001', 'STUDENT', 'Default learner'),
    ('a0000000-0000-0000-0000-000000000002', 'CONTENT_REVIEWER', 'Reviews questions'),
    ('a0000000-0000-0000-0000-000000000003', 'CONTENT_MANAGER', 'Manages catalog'),
    ('a0000000-0000-0000-0000-000000000004', 'ADMIN', 'Administration'),
    ('a0000000-0000-0000-0000-000000000005', 'SUPER_ADMIN', 'Full access');

INSERT INTO permissions (id, code, description) VALUES
    ('b0000000-0000-0000-0000-000000000001', 'questions:review', 'Approve or reject questions'),
    ('b0000000-0000-0000-0000-000000000002', 'questions:publish', 'Publish approved questions'),
    ('b0000000-0000-0000-0000-000000000003', 'users:manage', 'Suspend or activate users'),
    ('b0000000-0000-0000-0000-000000000004', 'plans:manage', 'Edit subscription plans');

INSERT INTO role_permissions (role_id, permission_id) VALUES
    ('a0000000-0000-0000-0000-000000000002', 'b0000000-0000-0000-0000-000000000001'),
    ('a0000000-0000-0000-0000-000000000003', 'b0000000-0000-0000-0000-000000000001'),
    ('a0000000-0000-0000-0000-000000000003', 'b0000000-0000-0000-0000-000000000002'),
    ('a0000000-0000-0000-0000-000000000004', 'b0000000-0000-0000-0000-000000000001'),
    ('a0000000-0000-0000-0000-000000000004', 'b0000000-0000-0000-0000-000000000002'),
    ('a0000000-0000-0000-0000-000000000004', 'b0000000-0000-0000-0000-000000000003'),
    ('a0000000-0000-0000-0000-000000000004', 'b0000000-0000-0000-0000-000000000004'),
    ('a0000000-0000-0000-0000-000000000005', 'b0000000-0000-0000-0000-000000000001'),
    ('a0000000-0000-0000-0000-000000000005', 'b0000000-0000-0000-0000-000000000002'),
    ('a0000000-0000-0000-0000-000000000005', 'b0000000-0000-0000-0000-000000000003'),
    ('a0000000-0000-0000-0000-000000000005', 'b0000000-0000-0000-0000-000000000004');

INSERT INTO exams (id, code, name, status, default_language) VALUES
    ('c0000000-0000-0000-0000-000000000001', 'JEE', 'Joint Entrance Examination', 'ACTIVE', 'en'),
    ('c0000000-0000-0000-0000-000000000002', 'NEET', 'National Eligibility cum Entrance Test', 'ACTIVE', 'en'),
    ('c0000000-0000-0000-0000-000000000003', 'NDA', 'National Defence Academy', 'ACTIVE', 'en');
