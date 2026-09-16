# Flutter student app

Application ID / bundle ID: `in.claris.medycatalog`

The Flutter SDK is required to generate `android/` and `ios/` platform folders:

```bash
cd mobile
flutter create . --org in.claris --project-name medycatalog --platforms android,ios
flutter pub get
flutter run --dart-define=API_BASE_URL=http://10.0.2.2:8080
```

Use your machine LAN IP instead of `10.0.2.2` on a physical device.

`lib/` already follows Clean Architecture + Riverpod (auth and profile only in Phase 2).
