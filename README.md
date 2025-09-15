# Sprint 7: Secondary Feature Implementation

## Overview
BankBoosta's – Goal Getters FC mobile app (Android, Kotlin). This README documents what was delivered in Sprint 7 against the Sprint goals and FURPS quality attributes, with links to relevant modules, testing status, and identified gaps/next steps.

- Sprint goal: Implement user stories for secondary features, write unit tests, integrate components, and conduct integration testing.
- Codebase: Native Android (Kotlin), MVVM, Room (offline), Firebase (Auth/Firestore), WorkManager (sync), DI modules.

## Scope Delivered in Sprint 7
- Scheduling and event management (create, view, edit practices and matches)
- Attendance recording with simple tick-box UI
- Team lineups UI with drag-and-drop interactions (partial persistence)
- Player profile and basic stats surfaces (partial persistence)
- In-app notifications and broadcast lists (push path pending)
- Authentication via Firebase (Email/Password + Google)
- Offline-first repositories and background sync (Room + WorkManager)

## FURPS Audit Summary

### F — Functionality
- Coaches
  - Plan and schedule training sessions: Implemented.
    - Key: `HomeCalendarFragment`, `AddEventActivity`, `EventUpsertViewModel`, `EventRepository (online/offline)`, `EventFirestore`.
  - Record attendance with tick-boxes: Implemented.
    - Key: `AttendanceFragment`, `AttendanceViewModel`, `AttendanceDao`, `OnlineAttendanceRepository`, `OfflineAttendanceRepository`, `CombinedAttendanceRepository`.
  - Create/manage team line-ups with drag-and-drop: Partial (UI done, persistence TODOs remain).
    - Key: `LineupFragment`, `FormationActivity`, `FormationPitchView`, `PitchView`, `LineupPlayerGridAdapter`, `LineupRepository`.
  - Track player stats (goals, cards, performance): Partial (surfaces available; write flows to confirm).
    - Key: `PlayerStatsAdapter`, `PerformanceLog`, match-related viewmodels/activities.
  - Send instant alerts for changes: Partial (in-app broadcasts present; FCM push integration not yet wired).
    - Key: `NotificationsActivity`, `NotificationAdapter`, `Broadcast*` repositories.
- Players
  - View training and match schedules: Implemented. See calendar/event modules above.
  - Track own stats and attendance: Partial (profile/stats screens present; ensure persistence).
- Parents/Guardians
  - Real-time alerts and relevant info only: Partial (broadcasts exist; role scoping needs enforcement).
- Supporters
  - Public team pages for news/updates: Not in Android app scope in this repo.
- Role-based access
  - Role model present; centralized enforcement needs hardening.
    - Key: `UserRole` usage in `PlayerProfileFragment`, checks in ViewModels/Repos recommended.
- Admins
  - Create/edit teams, grant coach access: Implemented.
    - Key: `TeamViewerActivity`, `TeamDetail*`, `TeamRepository`, `TeamFirestore`.
- User authentication
  - Email/Password + Google: Implemented. Apple not applicable on Android.
    - Key: `AuthenticationService`, `GoogleAuthenticationClient`, `SignInActivity` (`googleSignIn()`), startup flows.
- Offline access
  - Offline repositories, Room cache, background sync: Implemented.
    - Key: `AppDatabase` + DAOs, `Combined*Repository`, `SyncWorker`, `SyncManager`, `SyncScheduler`.
- Data protection
  - Firebase Auth, role model present; parental consent handling not found; document and implement if required.

### U — Usability
- Mobile-first native UI: Implemented (fragments/activities, material components).
- Intuitive drag-and-drop formations: UI implemented; save/load flows pending.
- Simple tick-boxes for attendance: Implemented.
- Clean, modern interface with branding: Implemented across screens.
- Minimal steps for key tasks: Generally good; polish will follow once persistence gaps close.
- Support for non-technical users: Onboarding and simple patterns present; iterate after testing.

### R — Reliability
- Offline mode and recoverability: Implemented (Room + WorkManager sync).
- Auto-sync on reconnect: Implemented.
- Secure authentication/session: Firebase Auth used across flows.
- Real-time alerts delivery: In-app broadcast supported; FCM push service not yet integrated.

### P — Performance
- Responsive lists and screens: Implemented using RecyclerView and ViewModels.
- Scales for ~100 concurrent users: Client-side OK; backend handled by Firebase.
- Real-time updates and multiple notifications: In-app broadcasts OK; push integration pending.
- Background syncing avoids UI lag: Implemented with WorkManager.

### S — Supportability
- Modern, layered architecture (DI, MVVM, Repos). Clean separation of online/offline.
- Codebase organized for onboarding; some TODOs remain (notably lineup/formation persistence and FCM push).
- Prefer same team maintenance: documented structure supports continuity.

## Key Modules and Files
- UI
  - `app/src/main/java/com/ggetters/app/ui/central/views/HomeCalendarFragment.kt`
  - `app/src/main/java/com/ggetters/app/ui/central/views/AddEventActivity.kt`
  - `app/src/main/java/com/ggetters/app/ui/central/fragments/AttendanceFragment.kt`
  - `app/src/main/java/com/ggetters/app/ui/central/fragments/LineupFragment.kt`
  - `app/src/main/java/com/ggetters/app/ui/central/views/FormationActivity.kt`
  - `app/src/main/java/com/ggetters/app/ui/central/views/NotificationsActivity.kt`
- ViewModels
  - `EventUpsertViewModel.kt`, `AttendanceViewModel.kt`, `LineupViewModel.kt`, `HomeCalendarViewModel.kt`
- Data/Repositories
  - Offline: `AppDatabase.kt` + DAOs (EventDao, AttendanceDao, LineupDao, etc.)
  - Online: Firestore-backed repos (`Online*Repository.kt`, `*Firestore.kt`)
  - Combined: `Combined*Repository.kt`
- Auth/Startup
  - `AuthenticationService.kt`, `GoogleAuthenticationClient.kt`, `StartActivity.kt`, `SignInActivity.kt`
- Sync
  - `SyncWorker.kt`, `SyncManager.kt`, `SyncScheduler.kt`

## How to Run
1. Requirements
   - Android Studio (Giraffe+), JDK 17, Android SDK, Google Play Services.
   - Firebase project configured; `google-services.json` placed under `app/`.
2. Build & Install (CLI)
   - Windows PowerShell:
     ```powershell
     ./gradlew.bat clean assembleDebug
     ```
   - Install on device/emulator via Android Studio or ADB.

## Testing Status and How to Test
- Current automated tests: Only template examples found (`app/src/test/java/.../ExampleUnitTest.kt`, `app/src/androidTest/java/.../ExampleInstrumentedTest.kt`).
- Manual testing recommendations for Sprint 7:
  - Scheduling: Create/edit/delete events; verify calendar updates offline/online.
  - Attendance: Toggle tick-boxes; go offline → make changes → reconnect and verify sync.
  - Lineups: Drag players onto pitch; change formation; verify UI state persists in-memory (persistence WIP).
  - Notifications: Create broadcasts; verify `NotificationsActivity` list updates.
  - Auth: Sign in with Email/Password and Google; logout/login cycles.
  - Offline sync: Toggle airplane mode; create events/attendance; reconnect; verify combined repo sync.
- Add tests (next sprint):
  - Unit tests for `EventUpsertViewModel`, `AttendanceViewModel`, `LineupViewModel`, repositories (offline/online/combined).
  - Instrumentation tests for Attendance tick-box flows, calendar CRUD, lineup drag/drop interactions.
  - Integration tests for sync behavior (Room ↔ Firestore) and auth flow.

## Known Gaps / Next Steps
1. Lineups persistence
   - Implement save/load in `LineupRepository` and wire from `LineupFragment`/`FormationActivity` (resolve TODOs).
2. Player stats
   - Finalize write/update flows and persistence for goals/cards/performance; connect adapters and repositories.
3. Push notifications
   - Add `FirebaseMessagingService`, notification channels, topic/user subscriptions, and server-side triggers.
4. Role enforcement
   - Centralize role-based authorization checks; ensure parent/supporter scoped views and data filtering.
5. Data protection
   - Implement/document parental consent where needed; review PII access rules.
6. Tests
   - Build the test suite as outlined above; include CI job for unit/instrumentation tests.

## Release Notes (Sprint 7)
- Added: Scheduling CRUD, attendance tick-box UI with offline-first sync, lineup UI with drag/drop, in-app notifications list, Google/Email auth, background sync.
- Changed: Calendar and event flows integrated with combined repos.
- Known Issues: Lineup persistence, player stats write flows, push notifications pending; limited automated tests.

## Contact
- Team: BankBoosta's
- Maintainers: Prefer same development team post-launch. For onboarding, start with DI modules and repository boundaries under `app/src/main/java/com/ggetters/app/data/di` and `.../data/repository`.

## Sprint 7 Requirements → How This App Fulfills Them

1) Implement user stories related to secondary features
- Implemented in this sprint:
  - Scheduling/events CRUD: calendar + event creation/editing (`HomeCalendarFragment`, `AddEventActivity`, `EventUpsertViewModel`, `EventRepository`, `EventFirestore`).
  - Attendance tick-boxes: record/update attendance with offline support (`AttendanceFragment`, `AttendanceViewModel`, DAOs, CombinedAttendanceRepository).
  - Team lineups drag-and-drop: interactive UI to position players; persistence WIP (`LineupFragment`, `FormationActivity`, `FormationPitchView`, `LineupRepository`).
  - Player stats surfaces: profile and adapters available; write flows WIP (`PlayerProfileFragment`, `PlayerStatsAdapter`, `PerformanceLog`).
  - In-app notifications/broadcasts: list and view notifications (`NotificationsActivity`, `NotificationAdapter`, `Broadcast*` repos).
  - Authentication: Email/Password + Google Sign-In (`AuthenticationService`, `GoogleAuthenticationClient`, `SignInActivity`).
  - Offline mode and background sync: Room cache + WorkManager (`AppDatabase`, DAOs, `SyncWorker`, `SyncManager`).

2) Write unit tests to ensure code quality and functionality
- Current state: Minimal template tests present (`ExampleUnitTest.kt`, `ExampleInstrumentedTest.kt`).
- Actions taken: Manual test plan provided (see "Testing Status and How to Test").
- Next actions (queued for next sprint):
  - Unit tests: ViewModels (`EventUpsertViewModel`, `AttendanceViewModel`, `LineupViewModel`) and Repositories (online/offline/combined).
  - Mocks/fakes for Firestore/Room layers; add CI job to run tests.

3) Integrating implemented components and conducting integration testing
- Integration status:
  - UI ↔ ViewModel ↔ Repository wiring completed for Scheduling and Attendance (online/offline/combined paths).
  - Sync integration with WorkManager verified for events/attendance offline changes.
  - Lineups UI integrated; save/load flows partially integrated (TODOs remain).
  - Notifications list integrated with broadcast repositories; push delivery integration pending.
- Integration testing status:
  - Performed manual end-to-end checks across scheduling, attendance, auth, and offline sync.
  - Identified remaining integration items: lineup persistence, push notifications, role-based access enforcement.
