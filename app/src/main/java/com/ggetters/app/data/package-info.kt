package com.ggetters.app.data

/**
 * com.ggetters.app.data
 *
 * ├─ analytics/         // Managers and helpers for collecting app usage & custom events
 * ├─ auth/              // Firebase Authentication flows (login, registration, token management)
 * ├─ crash/             // CrashReporter and error-reporting setup (Sentry, Crashlytics, etc.)
 * ├─ local/             // Offline / on-device persistence (RoomDB)
 * │   ├─ dao/           // @Dao interfaces for querying and updating local tables
 * │   ├─ model/         // @Entity data classes representing SQLite tables
 * │   └─ AppDatabase.kt // RoomDatabase definition, migrations, and setup
 * ├─ remote/            // “Online” data sources & DTOs for network interactions
 * │   ├─ firestore/     // Classes handling FirebaseFirestore operations (collections, listeners, batch ops)
 * │   └─ model/         // Data-transfer objects (UserDto, TeamDto) matching Firestore documents
 * └─ repository/        // Offline-first “single source of truth” abstractions coordinating local and remote
 *     └─ UserRepository // Domain-friendly API to observe, save, and sync User data
 *
 */
