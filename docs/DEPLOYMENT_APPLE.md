# Apple App Store deployment plan

1. Apple Developer: Bundle ID `in.claris.medycatalog`.
2. Certificates, provisioning, App Store Connect.
3. Sign in with Apple, Push (APNs), IAP subscription group:
   - `in.claris.medycatalog.premium.yearly` (₹1,499, default)
   - `in.claris.medycatalog.premium.monthly` (₹199)
4. TestFlight, then App Review (demo account, subscription review notes).
5. Privacy nutrition labels, age rating, account deletion (Guideline 5.1.1).
6. No result-guarantee marketing copy.

Keep signing credentials out of Git.
