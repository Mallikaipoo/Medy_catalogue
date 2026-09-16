# Authentication architecture

```text
App → POST /auth/login or OAuth token
API → verify password / Google / Apple
API → load or create user_identities
API → access JWT + refresh token
App → Authorization: Bearer access
API → 401 when expired
App → POST /auth/refresh (rotate)
```

## Providers

| Provider | Notes |
| --- | --- |
| email | Register + login; BCrypt password |
| google | Verify ID token server-side with Google client ID |
| apple | Verify identity token against Apple JWKS; required on iOS if Google is offered |
| otp | Email OTP first; hashed, expiry, attempt cap. SMS later |
| guest | Creates a user with `guest` identity; upgrade merges email/Google/Apple onto the same `user_id` |

## Tokens

- Access: HS256 JWT, 15 minutes, claims `sub` (user id), `roles`.
- Refresh: 256-bit random, stored SHA-256 hashed, 30 days, rotated on use, reusable detection revokes the family.
- Logout revokes the presented refresh token.

## Guest upgrade

Authenticated guest calls `POST /auth/guest/upgrade` with email+password or a social token. If that identity already belongs to another user, the API returns a conflict — no silent merge of two long-lived accounts.

## Roles

Assigned in `user_roles`. New self-serve accounts receive `STUDENT` only. Student tokens cannot call `/api/v1/admin/**`.

## Local OTP

When `medycatalog.auth.expose-otp=true` (dev profile only), OTP request responses include `debugCode` so local testing does not need SMTP. Production must keep this flag false.
