-- MedyCatalog V4: subscriptions, store receipts, ads config.
-- Default commercial offer: yearly ₹1499 (highlighted), monthly ₹199.
-- Access is server-side. Grace keeps study running if a renewal is late.

CREATE TABLE subscription_plans (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code VARCHAR(32) NOT NULL UNIQUE,
    name VARCHAR(80) NOT NULL,
    description TEXT,
    highlight BOOLEAN NOT NULL DEFAULT FALSE,
    sort_order INT NOT NULL DEFAULT 0,
    active BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE plan_entitlements (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    plan_id UUID NOT NULL REFERENCES subscription_plans (id) ON DELETE CASCADE,
    code VARCHAR(64) NOT NULL,
    value_int INT,
    value_bool BOOLEAN,
    UNIQUE (plan_id, code)
);

CREATE TABLE plan_prices (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    plan_id UUID NOT NULL REFERENCES subscription_plans (id) ON DELETE CASCADE,
    platform VARCHAR(32) NOT NULL,
    period VARCHAR(16) NOT NULL,
    currency VARCHAR(8) NOT NULL DEFAULT 'INR',
    amount NUMERIC(10, 2) NOT NULL,
    store_product_id VARCHAR(128) NOT NULL,
    highlighted BOOLEAN NOT NULL DEFAULT FALSE,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    UNIQUE (platform, store_product_id)
);

CREATE INDEX idx_plan_prices_plan ON plan_prices (plan_id, platform);

CREATE TABLE user_subscriptions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    plan_id UUID NOT NULL REFERENCES subscription_plans (id),
    status VARCHAR(32) NOT NULL,
    platform VARCHAR(32) NOT NULL,
    store_product_id VARCHAR(128) NOT NULL,
    period_start TIMESTAMPTZ NOT NULL,
    period_end TIMESTAMPTZ NOT NULL,
    grace_until TIMESTAMPTZ,
    auto_renew BOOLEAN NOT NULL DEFAULT TRUE,
    latest_purchase_token_hash VARCHAR(64),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_user_subscriptions_user_status ON user_subscriptions (user_id, status);
CREATE INDEX idx_user_subscriptions_token ON user_subscriptions (latest_purchase_token_hash);

CREATE TABLE store_transactions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    subscription_id UUID REFERENCES user_subscriptions (id) ON DELETE SET NULL,
    platform VARCHAR(32) NOT NULL,
    event_type VARCHAR(32) NOT NULL,
    store_transaction_id VARCHAR(191) NOT NULL,
    purchase_token_hash VARCHAR(64) NOT NULL,
    payload TEXT,
    verified_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (platform, store_transaction_id)
);

CREATE TABLE user_practice_credits (
    user_id UUID PRIMARY KEY REFERENCES users (id) ON DELETE CASCADE,
    extra_starts INT NOT NULL DEFAULT 0,
    rewarded_on DATE,
    rewarded_today INT NOT NULL DEFAULT 0,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE ad_configs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    placement VARCHAR(64) NOT NULL,
    platform VARCHAR(32) NOT NULL,
    unit_id VARCHAR(128) NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    UNIQUE (placement, platform)
);

CREATE TABLE feature_flags (
    flag_key VARCHAR(64) PRIMARY KEY,
    enabled BOOLEAN NOT NULL DEFAULT FALSE,
    description TEXT
);

ALTER TABLE practice_sessions
    ADD COLUMN entitlement_snapshot TEXT,
    ADD COLUMN rewarded_attempt BOOLEAN NOT NULL DEFAULT FALSE;

INSERT INTO subscription_plans (id, code, name, description, highlight, sort_order, active) VALUES
    ('c1000000-0000-0000-0000-000000000001', 'FREE', 'Free', 'Daily practice with ads. Watch a rewarded ad for extra attempts.', FALSE, 0, TRUE),
    ('c1000000-0000-0000-0000-000000000002', 'PREMIUM', 'Premium', 'Unlimited practice, detailed explanations, ads off. Yearly billed once so access stays continuous.', TRUE, 1, TRUE);

INSERT INTO plan_entitlements (id, plan_id, code, value_int, value_bool) VALUES
    ('c1000000-0000-0000-0000-000000000011', 'c1000000-0000-0000-0000-000000000001', 'unlimited_practice', NULL, FALSE),
    ('c1000000-0000-0000-0000-000000000012', 'c1000000-0000-0000-0000-000000000001', 'daily_practice_sessions', 3, NULL),
    ('c1000000-0000-0000-0000-000000000013', 'c1000000-0000-0000-0000-000000000001', 'detailed_explanations', NULL, FALSE),
    ('c1000000-0000-0000-0000-000000000014', 'c1000000-0000-0000-0000-000000000001', 'ads_off', NULL, FALSE),
    ('c1000000-0000-0000-0000-000000000015', 'c1000000-0000-0000-0000-000000000001', 'rewarded_extra_attempts', NULL, TRUE),
    ('c1000000-0000-0000-0000-000000000016', 'c1000000-0000-0000-0000-000000000001', 'full_mocks', NULL, FALSE),
    ('c1000000-0000-0000-0000-000000000017', 'c1000000-0000-0000-0000-000000000001', 'ai_tutor_quota', 0, NULL),
    ('c1000000-0000-0000-0000-000000000018', 'c1000000-0000-0000-0000-000000000001', 'study_plan', NULL, FALSE),
    ('c1000000-0000-0000-0000-000000000021', 'c1000000-0000-0000-0000-000000000002', 'unlimited_practice', NULL, TRUE),
    ('c1000000-0000-0000-0000-000000000022', 'c1000000-0000-0000-0000-000000000002', 'daily_practice_sessions', NULL, NULL),
    ('c1000000-0000-0000-0000-000000000023', 'c1000000-0000-0000-0000-000000000002', 'detailed_explanations', NULL, TRUE),
    ('c1000000-0000-0000-0000-000000000024', 'c1000000-0000-0000-0000-000000000002', 'ads_off', NULL, TRUE),
    ('c1000000-0000-0000-0000-000000000025', 'c1000000-0000-0000-0000-000000000002', 'rewarded_extra_attempts', NULL, FALSE),
    ('c1000000-0000-0000-0000-000000000026', 'c1000000-0000-0000-0000-000000000002', 'full_mocks', NULL, TRUE),
    ('c1000000-0000-0000-0000-000000000027', 'c1000000-0000-0000-0000-000000000002', 'ai_tutor_quota', 50, NULL),
    ('c1000000-0000-0000-0000-000000000028', 'c1000000-0000-0000-0000-000000000002', 'study_plan', NULL, TRUE);

INSERT INTO plan_prices (id, plan_id, platform, period, currency, amount, store_product_id, highlighted, active) VALUES
    ('c1000000-0000-0000-0000-000000000031', 'c1000000-0000-0000-0000-000000000002', 'GOOGLE_PLAY', 'YEAR', 'INR', 1499.00, 'in.claris.medycatalog.premium.yearly', TRUE, TRUE),
    ('c1000000-0000-0000-0000-000000000032', 'c1000000-0000-0000-0000-000000000002', 'GOOGLE_PLAY', 'MONTH', 'INR', 199.00, 'in.claris.medycatalog.premium.monthly', FALSE, TRUE),
    ('c1000000-0000-0000-0000-000000000033', 'c1000000-0000-0000-0000-000000000002', 'APP_STORE', 'YEAR', 'INR', 1499.00, 'in.claris.medycatalog.premium.yearly', TRUE, TRUE),
    ('c1000000-0000-0000-0000-000000000034', 'c1000000-0000-0000-0000-000000000002', 'APP_STORE', 'MONTH', 'INR', 199.00, 'in.claris.medycatalog.premium.monthly', FALSE, TRUE);

-- Google sample / test unit IDs. Replace with live AdMob units before production.
INSERT INTO ad_configs (id, placement, platform, unit_id, enabled) VALUES
    ('c1000000-0000-0000-0000-000000000041', 'HOME_BANNER', 'ANDROID', 'ca-app-pub-3940256099942544/6300978111', TRUE),
    ('c1000000-0000-0000-0000-000000000042', 'HOME_BANNER', 'IOS', 'ca-app-pub-3940256099942544/2934735716', TRUE),
    ('c1000000-0000-0000-0000-000000000043', 'RESULT_INTERSTITIAL', 'ANDROID', 'ca-app-pub-3940256099942544/1033173712', TRUE),
    ('c1000000-0000-0000-0000-000000000044', 'RESULT_INTERSTITIAL', 'IOS', 'ca-app-pub-3940256099942544/4411468910', TRUE),
    ('c1000000-0000-0000-0000-000000000045', 'REWARDED_EXTRA_ATTEMPT', 'ANDROID', 'ca-app-pub-3940256099942544/5224354917', TRUE),
    ('c1000000-0000-0000-0000-000000000046', 'REWARDED_EXTRA_ATTEMPT', 'IOS', 'ca-app-pub-3940256099942544/1712485313', TRUE);

INSERT INTO feature_flags (flag_key, enabled, description) VALUES
    ('ads_enabled', TRUE, 'Master switch. Turn off if the ad network is down — practice still works.'),
    ('billing_sandbox', TRUE, 'Accept sandbox purchase tokens. Disable in production when Play/App Store verify is live.');
