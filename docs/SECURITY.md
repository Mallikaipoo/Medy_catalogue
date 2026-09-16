# Security architecture

## Transport and app

- TLS for all non-local environments; HSTS on API and admin.
- CORS allowlist is the admin origin only. Native mobile does not rely on CORS.
- Stateless API. Access JWT ~15 minutes. Refresh ~30 days, hashed at rest, rotated, revocable.
- Passwords: BCrypt. OTP and refresh tokens: SHA-256 hashes, never stored in plaintext.
- RBAC on the server. Hiding admin buttons in Flutter/Next is not authorization.
- Rate limits on login, OTP, password reset (in-memory in Phase 2; Redis in later hardening).
- Bean Validation + parameterized JPA. No string-concatenated SQL.
- Audit log for review, publish, suspend, price change (schema in V1; writers expand by phase).

## Anti-cheat (later practice/mock phases)

- Server stores `start_at` / `must_submit_by`. Device clock is display-only.
- Official score is computed from stored answer keys after submit.
- Reject impossible completion times and excessive submission rates.

## Secrets

| Location | Allowed |
| --- | --- |
| Flutter | API URL, OAuth client IDs, AdMob IDs |
| Spring env / Secrets Manager | JWT signing key, DB, AI, store APIs |
| Git | Never private keys, `.env`, keystores |

## Privacy (India DPDP + store rules)

- Collect only needed profile fields.
- Age gate via `date_of_birth`; parental consent rows for under 18.
- Account deletion request + delayed purge.
- Privacy Policy and Terms served from `/api/v1/legal/*` and static docs.
- Product analytics events carry no extra PII.

## Logging

Do not log passwords, tokens, OTP codes (except the dedicated local debug flag), or full payment payloads.
