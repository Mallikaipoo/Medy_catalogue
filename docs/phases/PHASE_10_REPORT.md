# Phase 10 report — Monetization

- Date: 2026-09-16
- Goal: Server-side Premium, yearly default, ads that never interrupt a live test.

## Completed

- Locked prices: **₹1,499 / year (default)** and **₹199 / month**.
- Flyway `V4__monetization.sql` (plans, entitlements, prices, subscriptions, receipts, ads, flags, practice credits).
- `GET /subscription/plans|me`, `POST /subscription/verify|restore|rewarded`.
- `GET /ads/config` (no PLAYER / EXAM / SUBMIT placements).
- `POST /webhooks/google-play` and `/app-store` (HMAC when secrets are set).
- Practice start checks daily limit; in-progress sessions snapshot entitlements and are never closed for billing.
- Flutter paywall, remaining-starts, opt-in rewarded extra start, non-blocking ad slots.
- 3-day grace after renewal failure.

## Files created or modified

- `backend/src/main/resources/db/migration/V4__monetization.sql`
- `backend/.../modules/billing/**`
- `backend/.../PracticeService.java`, `PracticeSessionEntity`, `PracticeSessionRepository`, `SecurityConfig`, `MedycatalogProperties`
- `mobile/lib/features/billing/**`, home / profile / practice setup / result
- Tests: `SubscriptionAccessTest`, `ReviewEntitlementFilterTest`, `SubscriptionControllerTest`, `mobile/test/billing_test.dart`

## Database changes

- Flyway: `V4__monetization.sql`
- Tables: `subscription_plans`, `plan_entitlements`, `plan_prices`, `user_subscriptions`, `store_transactions`, `user_practice_credits`, `ad_configs`, `feature_flags`
- `practice_sessions.entitlement_snapshot`, `practice_sessions.rewarded_attempt`

## APIs added

- `/api/v1/subscription/plans`, `/me`, `/verify`, `/restore`, `/rewarded`
- `/api/v1/ads/config`
- `/api/v1/webhooks/google-play`, `/app-store`

## Tests added

- Grace / cancel / refund access
- Review keeps detailed explanations from the **session snapshot**
- Ads placement policy blocks player
- Yearly ₹1499 highlighted on plans API

## Known issues

- Live Google Play / App Store purchase APIs are not called yet. Sandbox verify is on (`medycatalog.billing.sandbox=true`).
- AdMob SDK is not compiled in; slots render a non-blocking placeholder until `android/` / `ios/` exist.
- Store product IDs must be created in Play Console and App Store Connect before production IAP.

## How to test

```bash
cd infra
docker compose up -d
cd ../backend
./gradlew test
./gradlew bootRun
```

Sign in, open Premium, choose yearly (sandbox token). Start practice — it continues even if you later expire the row in `user_subscriptions`. Free users get 3 starts/day; rewarded endpoint adds one extra.

## Approval

Wait for approval before starting the next phase.
