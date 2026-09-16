# Google Play deployment plan

1. Reserve application ID `in.techgeneza.medycatalog`.
2. Play Console: app, content rating, Data Safety, privacy policy URL.
3. Play App Signing. Upload keystore is **not** in Git.
4. Closed testing track, then production AAB:

   `flutter build appbundle --flavor prod`

5. Store listing: **MedyCatalog** — Learn. Practice. Compete. Crack Your Exam.
6. Subscription products matching `plan_prices.store_product_id`.
7. AdMob / UMP consent if ads ship in the free tier.

Do not claim guaranteed rank, guaranteed selection, or 100% success.
