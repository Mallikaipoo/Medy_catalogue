# Phase 10 playbook — Monetization

## Goal

Configurable plans, IAP, server verification, ads config. Never trust the client for premium.

**Implemented** — see `PHASE_10_REPORT.md`. Default offer is yearly ₹1,499 with monthly ₹199 backup and 3-day grace.

## Database

`subscription_plans`, `plan_entitlements`, `plan_prices`, `user_subscriptions`, `store_transactions`, `ad_configs`

Webhooks: Google RTDN, Apple Server Notifications V2. Restore purchase. Grace and refund handling.
