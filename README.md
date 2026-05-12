# FinPilot-Android-Finance-App
FinPilot is a smart personal finance management Android application built with Kotlin and Jetpack Compose. It helps users track multi-source income, manage daily expenses, visualize spending habits, and monitor savings goals through an intuitive dashboard. Powered by Firebase Authentication, Firestore, MVVM architecture, and offline caching.

## Exchange rates
- Live FX rates come from OpenExchangeRates (base USD) and are cached for 6 hours.
- Set `EXCHANGE_RATES_APP_ID` in `gradle.properties` (or `~/.gradle/gradle.properties`) to enable live rates.
- Income/expense screens show a rate preview and include a "Refresh rates" button to force update.
