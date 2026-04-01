# LABA Firenze — app Android

Applicazione nativa per studenti della Libera Accademia di Belle Arti di Firenze. Interfaccia in **Jetpack Compose**, allineata alle funzionalità dell’app iOS e integrata con il backend istituzionale e i servizi di notifica.

## Funzionalità

- **Home**: saluto, indicatori rapidi, sezioni personalizzate (strumentazione, prenotazione aule, strumenti utili), notifiche e prossime lezioni dove previsto.
- **Esami**: esami prenotabili, superati e in corso; prenotazione integrata.
- **Corsi**: elenco corsi, dettagli, materiali dove esposti dall’API.
- **Seminari**: elenco, prenotazione e dettagli.
- **Profilo**: dati studente, impostazioni, uscita dall’account.

## Stack tecnico

- **Linguaggio**: Kotlin
- **UI**: Jetpack Compose, Material 3
- **Architettura**: MVVM, Hilt, ViewModel, Flow / StateFlow
- **Rete**: Retrofit, OkHttp
- **Sicurezza locale**: EncryptedSharedPreferences per dati di sessione sensibili
- **Notifiche**: Firebase Cloud Messaging (richiede `google-services.json` configurato nel modulo app)

Versioni target e SDK sono definite in `app/build.gradle.kts` (`compileSdk`, `minSdk`, `targetSdk`, `versionCode`, `versionName`).

## Struttura del codice (estratto)

```
app/src/main/java/com/laba/firenze/
├── ui/           # Schermate Compose, navigazione, tema
├── data/         # API, storage, repository
├── domain/       # Modelli di dominio
├── di/           # Moduli Hilt
├── service/      # Servizi (es. FCM)
├── MainActivity.kt
└── LABAApplication.kt
```

## Configurazione locale

1. Clonare il repository e aprire la cartella in Android Studio.
2. Nel file **`local.properties`** (non versionato, lo crea di solito l’IDE) definire almeno **`OAUTH_CLIENT_ID`** e **`OAUTH_CLIENT_SECRET`** per il client LogosUNI. Riferimento nomi chiavi: `local.properties.example`.
3. Sincronizzare Gradle.
4. **Firebase**: posizionare `google-services.json` nel modulo `app/` se si usano FCM/Analytics (file dalla console Firebase).
5. Altre chiavi opzionali per la build: vedere `app/build.gradle.kts` (`IMGBB_API_KEY`, `SUPERSAAS_API_KEY`, ecc.). Non committare segreti.

## Build ed esecuzione

```bash
./gradlew :app:assembleDebug
./gradlew :app:installDebug
```

Emulatore o dispositivo con USB debugging abilitato; JDK e Android SDK come richiesto dalla versione di Android Studio in uso.

## Firma release e Google Play

L’**Android App Bundle** di produzione si genera con:

```bash
./gradlew :app:bundleRelease
```

L’output si trova in `app/build/outputs/bundle/release/`.

La firma **release** usa il file `keystore.properties` nella root del progetto Android (non versionato). Copiare `keystore.properties.example` e compilare i campi con il keystore di **upload** registrato in Play Console. Verificare che l’impronta del certificato corrisponda a quella attesa da Google prima di caricare l’AAB.

Senza `keystore.properties` valido, la build release può risultare firmata con il keystore di debug e **non** essere accettata da Play.

Non condividere password, alias o file keystore in repository o documentazione pubblica.

## Test

```bash
./gradlew test
./gradlew connectedAndroidTest
```

## Licenza e proprietà

Progetto proprietario **LABA Firenze**. Uso e distribuzione secondo le policy interne dell’istituto.

Per supporto operativo rivolgersi ai canali interni LABA (es. `supporto@laba.biz`).
