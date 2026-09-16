# Subscription architecture

Source of truth is `user_subscriptions` after **server** verification of Google Play or the App Store. The client never sends “I am premium”; it sends a purchase token or transaction id.

## Store integration (Phase 10)

- Google Play Developer API + Real-Time Developer Notifications.
- Apple App Store Server API + Server Notifications V2.
- Events: new, renew, cancel, expire, refund, grace, billing retry, restore.

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

Limited daily practice, selected questions, basic explanations, limited mocks, ads (remote `ad_configs`).

## Prices

Monthly ₹149–299 and annual ₹999–1999 live in `plan_prices` (platform, currency, `store_product_id`, amount). Admin can change them. The app displays API prices only.

## Ads

Rewarded ads may grant extra practice **attempts**. They must never grant XP, ranks, or official mock scores. No ads during an active exam or immediately before answer submit. Premium users do not see normal ads.
