# Goal Getters — Testing Branch README

A quick, plain‑English guide to what’s in this branch and how to run it.

---

## What’s inside

**Regression (instrumented, Firestore‑safe)**

* **`RegressionTests`**: sanity checks for package integrity, app context/resources, intent handling, and tiny UI probes.
* Uses short timeouts to avoid flakiness; key constants include `SHORT_WAIT = 1s`, `MEDIUM_WAIT = 2s`, `MAX_PERFORMANCE_MS = 5s`.

**Unit tests (pure JVM)**

* **`DevClassTest`**, **`SomeViewModelTest`**, **`UserRepositoryTest`**.
* Tooling: JUnit 5 (Jupiter), MockK, Kotlin Coroutines Test, and Robolectric (for Android bits without an emulator).
* Patterns: mock static `Log` calls, use a TestDispatcher to control coroutines, verify LiveData/Flow emissions deterministically.

**Integration tests**

* **`MainScreenEspressoTest`** (instrumented Espresso):

  * *BasicInstrumentationTest*: framework/context/package checks + valid launch intent.
  * *FirestoreSafeActivityTest*: launches Activities with graceful handling/logging if Firestore isn’t configured.
  * *MinimalTest*: ultra‑light UI render probes.
* **`TeamRepoIntegrationTest`** (JVM): CRUD + validation against a fake/in‑memory repo (create/read/update/delete, not‑found, invalid input, sequential ops).

> **Firestore note:** Instrumented tests are written to keep running and log warnings if Firestore is offline or misconfigured. They should not hard‑fail on backend hiccups.

---

## How to run

> For **instrumented** tests you need an emulator/device. **Unit/Robolectric** tests run on the JVM (no device needed).

**All unit (JVM) tests**

```bash
./gradlew test
# or variant‑specific
./gradlew testDebugUnitTest
```

**All instrumented (Android) tests**

```bash
./gradlew connectedAndroidTest
# or variant‑specific
./gradlew connectedDebugAndroidTest
```

**Run a single test class (example)**

```bash
./gradlew connectedDebugAndroidTest \
  -Pandroid.testInstrumentationRunnerArguments.class=com.ggetters.app.RegressionTests
```

(Adjust package/class to your project path.)

---

## What to expect

* **Regression:** quick smoke of install/config/intent/UI with short time caps.
* **Unit:** fast, deterministic checks for constructor safety, interactions, and coroutine/Livedata/Flow behavior.
* **Integration:** Espresso proves the app starts and renders minimal UI; repo tests exercise CRUD + validation in memory.

---

## CI quick start (GitHub Actions)

```yaml
- name: Unit tests
  run: ./gradlew --no-daemon test

- name: Instrumented tests
  run: ./gradlew --no-daemon connectedDebugAndroidTest
```

(Tip: allow time to boot and unlock the emulator before `connected*` tasks.)

---

## Troubleshooting

* **Package / launch intent fails** → check `applicationId` and launcher `Activity` in the manifest.
* **Firestore timeouts** → expected to be handled; see logs for ⚠️ warnings while tests proceed.
* **Mock vs real logic** → unit tests use mocks; for deeper coverage, swap in a fake/in‑memory DAO/repo in test fixtures.

---

