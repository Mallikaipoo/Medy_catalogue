# Admin panel architecture

Next.js (App Router, TypeScript, Tailwind) talks only to `/api/v1/admin`. There is no second student database.

## Screens (Phase 11)

- Dashboard: users, DAU, questions, tests, revenue, subscriptions
- Question editor and review queue (Approve / Reject / request changes)
- Bulk CSV/Excel import with validation; no partial publish of invalid batches
- Exam / pattern / marking editor
- Mock scheduler (publish / unpublish)
- User search, suspend, activate, subscription status
- Plans, prices, promotional offers
- Ad configuration
- Notification composer
- AI generation jobs
- Audit log

Publish is a separate permission from Approve. Super Admin assigns roles.

UI hiding is not security. Every admin route checks `user_roles` on the server.

Local placeholder lives in `admin/` until Phase 11.
