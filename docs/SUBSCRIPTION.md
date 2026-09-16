# Subscription architecture

Source of truth is `user_subscriptions` after **server** verification of Google Play or the App Store. The client never sends “I am premium”; it sends a purchase token or transaction id.

## Locked prices (Phase 10)

| Plan | Price | Why |
|------|--------|-----|
| **Premium yearly (default)** | **₹1,499 / year** | One charge, fewer failed cards, fewer access drops |
| Premium monthly | ₹199 / month | Optional. 12 × ₹199 = ₹2,388 |

Store fee is typically 15–30%. Yearly take-home is about ₹1,050–1,275.

Admin can change amounts in `plan_prices` later. Flutter only displays API prices.

Store product IDs:

- `in.claris.medycatalog.premium.yearly`
- `in.claris.medycatalog.premium.monthly`

## Smooth access (do not interrupt a live test)

- **3-day grace** after `period_end` if renewal is late (`GRACE` / `BILLING_RETRY`).
- Entitlements are **snapshotted** on practice start. Answer / submit / review of that session keep working even if the plan lapses mid-test.
- Canceled-but-paid still works until `period_end`.
- Refund / revoke ends premium immediately for **new** starts only.
- Ads never render on the question player, during an exam, or on Submit.
- Result ads are a non-blocking banner under the score, not a full-screen interstitial in front of Continue.
- `ads_enabled` feature flag can disable the network without taking practice down.
- Rewarded ads grant extra **practice starts** only (max 5/day). Never XP, ranks, or official scores.

## Store integration

- Google Play Developer API + Real-Time Developer Notifications.
- Apple App Store Server API + Server Notifications V2.
- Events: new, renew, cancel, expire, refund, grace, billing retry, restore.
- Until live store keys exist, `medycatalog.billing.sandbox=true` accepts verified sandbox tokens mapped to the product IDs above.

## Entitlements

Rows in `plan_entitlements`, not Flutter `if (premium)`:

- Unlimited practice
- Full question bank
- Detailed explanations
- AI tutor quota
- Personalised study plan
- Full mock tests
- Ads off

## Free tier

3 practice starts per IST day, simple explanations, ads (remote `ad_configs`), rewarded extra attempts.
