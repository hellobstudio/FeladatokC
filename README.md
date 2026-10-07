# Feladatok – egyszerű teendőlista Android app

Kotlin + Jetpack Compose. Az adatok a telefonon tárolódnak (nincs internet, nincs fiók).

## Funkciók
- Feladat rögzítése: cím, megjegyzés, határidő, fontosság (Sürgős / Normál / Ráér)
- Lista két nézetben: **Dátum** (Lejárt / Ma / Holnap / Később / Nincs határidő)
  vagy **Sürgősség** (Sürgős → Normál → Ráér, azon belül dátum szerint)
- Késznek jelölés pipával (a kész feladatok a lista alján, áthúzva), kész elrejtése
- Lejárt határidő pirossal; gyors határidő-gombok (Ma / Holnap / +1 hét)
- Szerkesztés / törlés a kártyára koppintva; kész feladatok tömeges törlése a menüből
- Sötét mód automatikusan

## APK készítése GitHubon
1. Hozz létre egy új GitHub repót, és töltsd fel ide a zip **tartalmát** (a `build.gradle.kts`-nek a repo gyökerében kell lennie).
2. Push után az **Actions** fül → *APK build* fut le (kb. 3–5 perc).
3. A lefutott workflow aljáról töltsd le a **Feladatok-APK** artifactot, csomagold ki, és telepítsd az `app-debug.apk`-t a telefonra (engedélyezd az ismeretlen forrásból telepítést).

Helyi build (Android Studio): nyisd meg a mappát, és futtasd az `app` modult.
