### **Project README (GoalGetters-Application/README.md)**

# ⚽ GoalGetters FC — Club Management Application

GoalGetters FC is a **mobile-first, offline-first football club management app** built with **Kotlin Android**.
It unifies player tracking, attendance, lineup planning, and communication — replacing spreadsheets and chats with one hub.

---

## 🚀 Features

### Coaches

* Create/manage teams with join codes
* Plan matches and training sessions
* Track attendance, goals, cards, substitutions
* Send push alerts to players and parents

### Players

* View sessions and matches
* Log attendance and see season stats

### Parents

* View only their child’s data
* Receive push notifications for schedule changes

### Supporters

* View public fixtures and results only

---

## 🧩 Architecture

| Layer         | Technology                         |
| ------------- | ---------------------------------- |
| UI            | Kotlin (MVVM) with XML/Compose     |
| Local Storage | RoomDB (offline-first)             |
| Cloud         | Firebase Firestore                 |
| Auth          | Firebase Auth (Email + Google SSO) |
| Sync          | WorkManager two-way sync           |
| Notifications | Firebase Cloud Messaging           |
| Analytics     | Firebase Performance + Crashlytics |

---

## 🧠 How It Works

1. **Offline-first RoomDB** stores all core entities locally.
2. **SyncManager (WorkManager)** uploads dirty entities to Firestore when online.
3. **Firestore listeners** stream remote updates back to RoomDB.
4. **Cloud Functions + FCM** send push alerts to the right users.

---

## ⚙️ Getting Started

### Prerequisites

* Android Studio Jellyfish (or newer)
* Firebase project (Firestore, Auth, FCM, Performance)
* Google Services JSON configured in `/app`

### Setup

```bash
git clone https://github.com/GoalGetters-FC/GoalGetters-Application.git
cd GoalGetters-Application
```

1. Open in **Android Studio**
2. Add your **google-services.json** file
3. Sync Gradle
4. Run on emulator or device

---

## 🦯 Project Wiki

This repository’s detailed documentation (sprints, architecture, videos) is hosted in the **Wiki**.

🔗 [Open the GoalGetters Wiki →](https://github.com/GoalGetters-FC/GoalGetters-Application/wiki)

---

## 👥 Team

| Member      | Student No. | Role           |
| ----------- | ----------- | -------------- |
| Dean Gibson | ST10326084  |                |
| Matt        | —           |                |
| Fortune     | —           |                |
| Musa        | —           |                |


---

## 📚 References

* Android Developers. (2025). *Dependency injection in Android.*
  [https://developer.android.com/training/dependency-injection](https://developer.android.com/training/dependency-injection)
* App Dev Insights. (2024). *Repository Design Pattern in Kotlin.*
  [https://medium.com/@appdevinsights/repository-design-pattern-in-kotlin-1d1aeff1ad40](https://medium.com/@appdevinsights/repository-design-pattern-in-kotlin-1d1aeff1ad40)
* Firebase Docs. (2025). *Offline data and Firestore sync.*
  [https://firebase.google.com/docs/firestore/manage-data/enable-offline](https://firebase.google.com/docs/firestore/manage-data/enable-offline)

---

> For sprint-by-sprint progress and videos, visit the [Wiki](https://github.com/GoalGetters-FC/GoalGetters-Application/wiki).

---
