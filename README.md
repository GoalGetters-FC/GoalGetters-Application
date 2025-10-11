# Sprint 9: Performance and Scalability Report

## Overview

GoalGetters FC is an offline-first Android app built with **Kotlin**, **RoomDB**, and **Firebase Firestore** for real-time data sync and authentication. This sprint focused on improving performance, stability, and scalability using Firebase Performance Monitoring, Crashlytics, and optimized local database schema design.

---

## 1. Performance Testing

Firebase Performance Monitoring was integrated to trace and benchmark key repository operations. Custom traces confirmed areas of improvement and validated performance gains.

### Sample Trace Results

| Operation                 | Duration | Notes                                         |
| ------------------------- | -------- | --------------------------------------------- |
| `teamrepo_sync`           | 1.80 s   | Heaviest sync task involving Firestore merges |
| `attendance_sync`         | 345 ms   | Acceptable latency for event-level sync       |
| `attendance_upsert`       | 263 ms   | Post-optimization stable below 300 ms         |
| `userrepo_hydrateForTeam` | 122 ms   | Reduced by indexing Room table                |
| `userrepo_sync`           | 121 ms   | Efficient with batched Firestore writes       |
| `attendance_getById`      | 2 ms     | Local read speed optimal via Room             |
| `attendance_getByEventId` | 763 μs   | Near-instant cached retrieval                 |

---

## 2. Code and Database Optimizations

### a. Indexed Entities

Indices were added to the RoomDB schema to improve query lookup times and avoid full table scans.

```kotlin
@Entity(
    tableName = "user",
    foreignKeys = [
        ForeignKey(
            entity = Team::class,
            parentColumns = ["id"],
            childColumns = ["team_id"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["id"], unique = true),
        Index(value = ["auth_id", "team_id"], unique = true),
        Index(value = ["team_id"])
    ]
)
```

**Result:** Hydration and user-sync calls dropped from ~350ms to ~120ms.

### b. Repository Caching

RoomDB now acts as a first-level cache. Reads resolve locally, while writes flag records as `dirty` for deferred Firestore sync via `WorkManager`. This minimizes remote reads and improves offline performance.

### c. Query Streamlining

* Replaced multiple per-document Firestore reads with batch retrievals.
* Limited snapshot listeners to active sessions only.
* Reduced redundant LiveData observers.

---

## 3. Crash and Stability Improvements

Firebase Crashlytics logs showed three recurring issues prior to this sprint:

* `RoomConnectionManager.onMigrate`: Fixed by updating migration logic.
* `RoomConnectionManager.checkIdentity`: Caused by missing composite key, now resolved with proper indexing.
* `HomeTeamViewModel.getCurrentUserAuthId`: Fixed null pointer via safe calls and defensive coding.

**Crash-free session rate:** Improved from **~70%** to **87%** in the last 90 days.

---

## 4. Caching and Sync Strategy

### Local Caching (RoomDB)

* Player, Team, and Attendance entities cached offline.
* Auto-sync triggered by WorkManager when network available.
* Data flagged via `isDirty` column until Firestore sync confirms success.

### Remote Sync (Firestore)

* Firestore serves as source of truth for multi-device consistency.
* addSnapshotListener() merges remote updates into RoomDB in real time.

---

## 5. Scalability & Auto-Scaling Configuration

Since the project uses Firebase (no custom backend), scaling is handled automatically by Google Cloud Infrastructure:

* **Firestore:** Serverless scaling for high concurrent reads/writes.
* **Cloud Functions:** Auto-scales based on invocations (used for push notifications).
* **FCM:** Handles unlimited concurrent message delivery.

---

## 6. Video Walkthrough (to upload)

**Content Outline:**

1. Show Firebase dashboard traces.
2. Show RoomDB schema changes.
3. Demonstrate offline caching + sync cycle.
4. Review Crashlytics improvement.
5. Summarize scalability architecture.

---

## 7. GitHub Repository

**Repository:** [GoalGetters FC - Android](https://github.com/GoalGettersFC/goalgetters-android)
**Status:** Public
**Branch:** `staging` (Sprint 9 release)

---

## References

* Android Developers. (2025). *Offline persistence and caching with Room*. [online] Available at: [https://developer.android.com/training/data-storage/room](https://developer.android.com/training/data-storage/room)
* Firebase Docs. (2025). *Performance Monitoring and Crashlytics*. [online] Available at: [https://firebase.google.com/docs](https://firebase.google.com/docs)
* App Dev Insights. (2024). *Repository Design Pattern in Kotlin*. Medium. [online] Available at: [https://medium.com/@appdevinsights/repository-design-pattern-in-kotlin-1d1aeff1ad40](https://medium.com/@appdevinsights/repository-design-pattern-in-kotlin-1d1aeff1ad40)
