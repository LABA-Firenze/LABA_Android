# PLAN: Android = Clone identico di iOS LABA

> Analisi comparativa completa per rendere l'app Android una copia fedele di iOS.  
> **Esclusi**: minigiochi (Battaglia, Forza4, LABAdoku, LABottega, ecc.) — solo su iOS.

---

## PARTE 1 — ROOT E NAVIGAZIONE

### 1.1 Badge tab Esami
- [x] **1.1.1** Aggiungere badge `"!"` sul tab Esami quando `bookableExamsCount > 0`
- [x] **1.1.2** Expose `bookableExamsCount` dal SessionViewModel/Repository (se non esiste)
- [x] **1.1.3** `NavigationBarItem` per Esami: mostrare `Badge("!")` condizionale sopra l'icona

### 1.2 Badge tab Seminari/Attività
- [x] **1.2.1** Badge numerico sul tab Attività: valore = `bookableSeminarsCount`
- [x] **1.2.2** Mostrare badge solo se `> 0` (come iOS)
- [x] **1.2.3** `NavigationBarItem` per Attività: `Badge(bookableSeminarsCount.toString())`

### 1.3 ensureTabInBar (deep link da notifica)
- [x] **1.3.1** Quando una push apre un documento (tipo 0=Programmi, tipo 1=Dispense + oid): inserire temporaneamente il tab corretto nella barra se non presente
- [x] **1.3.2** Navigare al documento e mostrare il tab attivo
- [x] **1.3.3** Comportamento identico a iOS `ensureTabInBar` in NavigationManager

### 1.4 Struttura tab
- [x] **1.4.1** Verificare ordine tab: Home, Esami, Corsi, Attività, Profilo (+ hidden: Dispense, Programmi, Regolamenti, Tesi)
- [x] **1.4.2** Label tab: "Bacheca", "Esami", "Corsi", "Attività", "Profilo" — confrontare con iOS

---

## PARTE 2 — HOME

### 2.1 Hero
- [x] **2.1.1** Frasi cicliche identiche (verificare da `heroPhraseCycle` / Localizable)
- [x] **2.1.2** Pattern selezionabili: wave, dots, grid, particles, circles, rays, ripple — stessi nomi e aspetto
- [x] **2.1.3** Animazione cambio frase e pattern come iOS

### 2.2 KPI Cards
- [x] **2.2.1** Tre card: Esami passati, Da sostenere, CFA
- [x] **2.2.2** Colori, bordi, animazioni (scale on appear) identici
- [x] **2.2.3** Easter egg: tap multipli rapidi sulla card centrale → sheet/dialog con elenco esami (come iOS)
- [x] **2.2.4** Haptic feedback su tap (se supportato)

### 2.3 Sezione Prossimi esami
- [x] **2.3.1** Visibile solo in finestre di sessione (stessa logica iOS)
- [x] **2.3.2** Date e formattazione come iOS

### 2.4 Lezioni oggi
- [x] **2.4.1** "Lezioni di oggi" + bottone "Visualizza orario completo"
- [x] **2.4.2** Stati vuoti: messaggi identici

### 2.5 PerTeSection
- [x] **2.5.1** Label: "Valutazione finale", "Simula la tua media" (o stringhe localizzate iOS)
- [x] **2.5.2** Icone e layout come iOS

### 2.6 ServiziSection
- [x] **2.6.1** "Service LABA", "Prenota le aule", "Biblioteca" — stessi label e flussi
- [x] **2.6.2** Verificare che StrumentazioneScreen → Service LABA sia mappato correttamente

### 2.7 Personalizzazione ordine sezioni
- [x] **2.7.1** HomeSectionOrderScreen: drag & drop, stessi ID sezione
- [x] **2.7.2** Persistenza ordine identica

### 2.8 Refresh e stati
- [x] **2.8.1** Pull-to-refresh con haptic al completamento
- [x] **2.8.2** Messaggi notifiche in hero (reformatMessaggioHome — da verificare su iOS)
- [x] **2.8.3** NetworkMonitor: UI per offline (banner o toast)

---

## PARTE 3 — SESSIONE STUDIO (mini player)

### 3.1 SessionMiniPlayerPill globale
- [ ] **3.1.1** Creare Composable `SessionMiniPlayerPill` che mostra tempo trascorso + stato
- [ ] **3.1.2** Posizionare pill sopra la bottom bar (dentro Scaffold, sopra bottomBar)
- [ ] **3.1.3** Visibile solo quando `FocusStudioStore` (o equivalente) ha sessione attiva
- [ ] **3.1.4** Tap su pill → navigare a SessioneStudioScreen (o aprire flow sheet)
- [ ] **3.1.5** Persistenza sessione attiva tra schermate (stato globale)

### 3.2 SessionFlowSheet
- [ ] **3.2.1** Da pill: aprire modal/bottom sheet con timer, pause, stop (come iOS)
- [ ] **3.2.2** "Riprendi" da landing SessioneStudio quando sessione già attiva

---

## PARTE 4 — PROFILO

### 4.1 Foto profilo
- [x] **4.1.1** Upload con PhotosPicker (o ImagePicker Android) → ImgBB
- [x] **4.1.2** Stessi campi: nome upload, fallback se ImgBB non configurato
- [x] **4.1.3** Avatar circolare, placeholder quando assente

### 4.2 ProfileAchievementWidget
- [x] **4.2.1** Stato "Traguardi disattivati": overlay con lucchetto, messaggio identico
- [x] **4.2.2** Tap → AchievementsScreen quando abilitati
- [x] **4.2.3** Layout gradient, bordi, dimensioni come iOS

### 4.3 Sezione Risorse (link dinamici)
- [x] **4.3.1** Costruire da `hiddenTabs` (Dispense, Programmi, Regolamenti, Tesi) se presenti
- [x] **4.3.2** Stessi link: Segreteria (mailto), WhatsApp, Sito LABA, Pagamento DSU Toscana
- [x] **4.3.3** URL identici a iOS

### 4.4 Debug
- [x] **4.4.1** Sheet/dialog con input codice 4 cifre (0526) prima di aprire DebugScreen
- [x] **4.4.2** Stesso flusso di scoperta (bottone Debug in Azioni → dialog 0526)

### 4.5 Logout
- [x] **4.5.1** Dialog conferma: "Vuoi davvero uscire?" con Annulla / Esci
- [x] **4.5.2** Comportamento identico

### 4.6 Altri elementi
- [x] **4.6.1** Rimuovere/nascondere riga "Apple Watch" (N/A Android)
- [x] **4.6.2** Tessera studente, Anagrafica, Gruppo: layout e testi come iOS

---

## PARTE 5 — NOTIFICHE E INBOX

### 5.1 Struttura Inbox
- [x] **5.1.1** Header con link "Avvisi sui caricamenti" → lista completa avvisi (equivalente NotificheView)
- [x] **5.1.2** InboxNotificationsScreen = NotificheInboxView
- [x] **5.1.3** Sub-vista "Avvisi" con toggle "Solo non lette" e "Segna tutte come lette"

### 5.2 Azioni su notifica
- [x] **5.2.1** Swipe: Elimina (rosso), Segna letta (verde), Segna non letta (arancione) — context menu
- [x] **5.2.2** Context menu con stesse opzioni di iOS
- [x] **5.2.3** Icone: checkmark.seal.fill (letta) vs envelope.badge (non letta)

### 5.3 Dettaglio notifica
- [x] **5.3.1** NotificationTapDetailScreen = NotificaDetailView
- [x] **5.3.2** Pulsanti "Letta" / "Da leggere" con stesse azioni
- [x] **5.3.3** Apertura link/documento da notifica (via deep link tipo/oid)

### 5.4 Deep link da push
- [x] **5.4.1** Apertura documento per tipo/oid: tipo 0 → Programmi, tipo 1 → Dispense
- [x] **5.4.2** Navigare a tab corretto + documento (ensureTabInBar)
- [x] **5.4.3** Ignorare payload minigiochi (Battaglia, Forza4, LABArola, Amici)

### 5.5 Stati vuoti
- [x] **5.5.1** "Nessuna notifica" con icona e messaggio coerenti con iOS

---

## PARTE 6 — ESAMI

### 6.1 Badge tab
- [x] Vedi 1.1 (già implementato)

### 6.2 Chips e filtri
- [x] **6.2.1** Chips anno: 1, 2, 3 (o 1, 2 per magistrali)
- [x] **6.2.2** Filtro: Tutti / Sostenuti / Non sostenuti
- [x] **6.2.3** Ricerca con debounce (300ms come iOS)

### 6.3 Gruppi esami
- [x] **6.3.1** Regolari, Attività a scelta, Tesi finale — stessi titoli e logica
- [x] **6.3.2** Separatori visivi come iOS

### 6.4 Formato voti
- [x] **6.4.1** 30L, ID, — (trattino) come displayGrade
- [x] **6.4.2** Colori e stile per voto passato/non passato

### 6.5 ExamDetailView
- [x] **6.5.1** Prenota esame: stesso flusso e messaggi
- [x] **6.5.2** BookedExamsView: "Esami prenotati" con stessa UI
- [x] **6.5.3** GradeTrendView: grafico andamento voti identico

---

## PARTE 7 — SEMINARI / ATTIVITÀ

### 7.1 Badge tab
- [x] Vedi 1.2

### 7.2 Tab e filtri
- [x] **7.2.1** Tab: Seminari | Attività integrative
- [x] **7.2.2** Filtri: Tutti, Prenotabili, Frequentati
- [x] **7.2.3** Testi stati vuoti identici ("Nessun seminario...")

### 7.3 Tirocini (Attività integrative)
- [x] **7.3.1** Vuoto: "Quando inizierai un tirocinio, lo troverai qui insieme alle ore registrate e ai CFA ottenuti."
- [x] **7.3.2** Card tirocinio: descrizione, anno, date, CFA, Relazione finale, Modulo ore, Biennio/Triennio

### 7.4 SeminarDetailView
- [x] **7.4.1** Prenota seminario: conferma + alert warning (seminario pieno via campo `warning`)
- [x] **7.4.2** Status pill: Frequentato, Non convalidato, Prenotato, Non prenotabile, Prenotabile
- [x] **7.4.3** Banner e messaggi come iOS

---

## PARTE 8 — DOCUMENTI (Programmi, Dispense, Regolamenti)

### 8.1 Deep link
- [x] Vedi 1.3 e 5.4

### 8.2 ProgrammiView / MaterialiView
- [x] **8.2.1** Lista documenti, apertura PDF
- [x] **8.2.2** Stati vuoti e errori come iOS ("Nessun programma", "Quando disponibili...", "Impossibile caricare")

### 8.3 DispenseView
- [x] **8.3.1** Stessa struttura
- [x] **8.3.2** DocumentViewerScreen con stesso comportamento

### 8.4 RegolamentiView
- [x] **8.4.1** Lista regolamenti, link o PDF
- [x] **8.4.2** Testi identici

---

## PARTE 9 — TESI

### 9.1 ThesisDocsView
- [x] **9.1.1** Stati: non abilitato (placeholder pergamena), in corso (esami), conclusa (isGraduated → link pergamena)
- [x] **9.1.2** Link a pergamena se disponibile (solo quando isGraduated, altrimenti "Disponibile dopo la discussione")

### 9.2 PergamenaRitiroView
- [x] **9.2.1** PergamenaScreen: layout e contenuti come iOS (Panoramica, Pagamento, Consegna)
- [x] **9.2.2** Messaggi e CTA identici

---

## PARTE 10 — ASPETTO E IMPOSTAZIONI

### 10.1 Tema
- [x] **10.1.1** Sistema / Chiaro / Scuro — stesse label
- [x] **10.1.2** Persistenza in SharedPreferences (AppearancePreferences)

### 10.2 Colori accent
- [x] **10.2.1** Stessa palette (Sistema, Pesca, Lavanda, Menta, Sabbia, Cielo + Blu LABA, Il lato oscuro, Un rosso un po' bruttino)
- [x] **10.2.2** Nomi colori identici a iOS

### 10.3 Pattern hero
- [x] **10.3.1** Elenco pattern: wave, dots, grid, particles, circles, rays, ripple
- [x] **10.3.2** Anteprima in AppearanceSettings → Animazione di sfondo
- [x] **10.3.3** Comportamento visivo come iOS

### 10.4 Personalizzazione barra
- [x] **10.4.1** NavigationCustomizationScreen: stessi tab add/remove
- [x] **10.4.2** Stessa logica hiddenTabs / activeTabs

### 10.5 Animazioni
- [x] **10.5.1** AnimationSettingsScreen: selezione pattern sfondo
- [x] **10.5.2** Pattern e persistenza come iOS

---

## PARTE 11 — SERVIZI (Funzionalità e Servizi)

### 11.1 ServiziScreen
- [x] **11.1.1** Toggle Orario delle lezioni, Traguardi, Esami prenotati
- [x] **11.1.2** Nessun toggle Minigiochi (solo iOS)
- [x] **11.1.3** BetaInfoKPI presente
- [x] **11.1.4** Disclaimer Orario/Traguardi identici a iOS

### 11.2 Layout e testi
- [x] **11.2.1** Label: Orario delle lezioni, Traguardi, Esami prenotati; Wi-Fi, Server Studenti, Guida alla stampa
- [x] **11.2.2** Messaggi informativi e disclaimer come iOS

---

## PARTE 12 — STRUMENTAZIONE / SERVICE LABA

### 12.1 ServiceLabaScreen / UserSimpleDashboard
- [x] **12.1.1** WebView attrezzatura.laba.biz (login su web; equivalenza funzionale)
- [x] **12.1.2** Accesso a Richieste, Prestiti, Report, Catalogo via sito
- [x] **12.1.3** Titolo "Service LABA"
- [x] **12.1.4** WebView full-screen

### 12.2 Biblioteca
- [x] **12.2.1** BibliotecaScreen → WebView biblioteca.laba.biz
- [x] **12.2.2** Contenuti e link come iOS

### 12.3 Prenotazione aule
- [x] **12.3.1** PrenotazioneAuleScreen (SuperSaas nativo o WebView)
- [x] **12.3.2** WebView/API come iOS

---

## PARTE 13 — GAMIFICATION

### 13.1 AchievementUnlockedToast
- [x] **13.1.1** Posizione: sopra bottom bar (in LABANavigation)
- [x] **13.1.2** Animazione slide-in/out, durata 4 secondi
- [x] **13.1.3** Layout: icona, titolo, +CFApp

### 13.2 AchievementDetailDialog
- [x] **13.2.1** Contenuti: titolo, descrizione, rarità, icona, progresso
- [x] **13.2.2** Stile e colori come iOS (ModalBottomSheet)

### 13.3 ProfileAchievementWidget
- [x] Vedi 4.2

### 13.4 AchievementsScreen
- [x] **13.4.1** Categorie, filtri, layout
- [x] **13.4.2** YearRecap (bottone "Il tuo Anno in LABA")
- [x] **13.4.3** trackSectionVisit("achievements")

---

## PARTE 14 — DETTAGLI UI E STRINGHE

### 14.1 Audit label (Per Te / Servizi)
- [x] **14.1.1** "Valutazione finale", "Simula la tua media" (PerTeSection)
- [x] **14.1.2** "Service LABA", "Prenota le aule", "Biblioteca" (ServiziSection)
- [x] **14.1.3** Label allineati a iOS

### 14.2 Stati vuoti
- [x] **14.2.1** Icona + messaggio + CTA dove previsto
- [x] **14.2.2** "Nessun esame", "Nessun seminario", "Nessuna notifica", ecc.

### 14.3 Messaggi di errore
- [x] **14.3.1** Rete: banner offline "Nessuna connessione internet"
- [x] **14.3.2** Auth: messaggi da SessionRepository/API
- [x] **14.3.3** Caricamento: "Errore nel caricamento", "Impossibile caricare"

### 14.4 Animazioni
- [x] **14.4.1** Slide/fade: 300ms (tween), 200ms fade
- [x] **14.4.2** Haptic: refresh, KPI tap, easter egg (HomeScreen, ProfileScreen)

### 14.5 Pill e chip
- [x] **14.5.1** Pill.kt: RoundedCornerShape 16dp, padding
- [x] **14.5.2** Status pills: colori per YEAR, GRADE, CFA, STATUS, ALERT

---

## PARTE 15 — PULIZIA E COERENZA

### 15.1 Rimozione Minigiochi
- [x] **15.1.1** Nessun tab/route Battaglia, Forza4, LABAdoku in navigazione
- [x] **15.1.2** Toggle Minigiochi assente in ServiziScreen (solo iOS)
- [x] **15.1.3** Deep link minigiochi ignorati (LABAFirebaseMessagingService)
- [x] **15.1.4** Nessun composable minigiochi nelle route principali

### 15.2 Nomi route e stringhe
- [x] **15.2.1** Route: "home", "exams", "courses", "seminars", "profile"
- [x] **15.2.2** Titoli: Bacheca, Esami, Corsi, Attività, Profilo
- [x] **15.2.3** Transizioni 300ms slide, 200ms fade

### 15.3 API e dati
- [x] **15.3.1** API v2/v3 via laba.apiVersion (AppearancePreferences)
- [x] **15.3.2** Modelli e mapping allineati
- [x] **15.3.3** bookableExamsCount, bookableSeminarsCount (MainActivityViewModel)

---

## PARTE 16 — COSE DA VERIFICARE (audit manuale)

### 16.1 Corsi
- [x] **CourseDetailScreen**: layout card, dettagli corso (docente, anno, CFA), propedeuticità, email docente
- [ ] Link materiali da corso (MaterialiScreen accessibile da altra route)
- [x] Anni in ExamInfoCard; gruppi usati in FullTimetableViewModel (filter gruppo)

### 16.2 Orari
- [x] **FullTimetableScreen**: settimanale, lezioni raggruppate per giorno
- [ ] Date picker (opzionale: attualmente mostra tutte le lezioni future)
- [x] **LessonDetailScreen**: docente, aula, orario (LessonDetailViewModel da LessonCalendarRepository)
- [x] Deep link laba://lesson/{id} in NavHost, MainActivity, AndroidManifest

### 16.3 Registratore lezioni
- [x] **RegistratoreLezioniScreen**: placeholder con messaggio “funzionalità in arrivo”
- [ ] Permessi mic (N/A per placeholder)

### 16.4 Calcolatori
- [x] **CalcolaVotoLaureaScreen**: campi media/30, formula ×110÷30, regole, esempio
- [x] **SimulaMediaScreen**: situazione attuale, aggiungi esami, media stimata, scenari 18–30

### 16.5 Agevolazioni
- [x] **AgevolazioniScreen**: lista partner, filtri categoria, BenefitRedeemScreen
- [x] WiFi, StudentServer, Printer guide: schermate con contenuti
- [x] **FAQScreen**: categorie da faq.json, domande espandibili (fallback se vuoto)

### 16.6 Privacy e sicurezza
- [x] **PrivacySecurityScreen**: Privacy Policy, Sicurezza, Gestione dati
- [ ] Termini e condizioni, cookie (eventuali link esterni)

---

## RIEPILOGO PRIORITÀ

| Priorità | Area | Task principali |
|----------|------|-----------------|
| 🔴 Alta | Nav + Badge | 1.1, 1.2 — badge Esami e Seminari |
| 🔴 Alta | Sessione Studio | 3.1, 3.2 — mini player pill |
| 🟠 Media | Profilo | 4.1, 4.2 — foto, achievement widget |
| 🟠 Media | Notifiche | 5.1–5.5 — inbox, swipe, deep link |
| 🟠 Media | ensureTabInBar | 1.3, 5.4, 8.1 — deep link documenti |
| 🟡 Bassa | UI polish | 2.2.3 easter egg, 14.x audit |
| 🟡 Bassa | Pulizia | 15.x — rimozione minigiochi |

---

*Documento generato da analisi comparativa iOS vs Android. Aggiornare man mano che le task vengono completate.*
