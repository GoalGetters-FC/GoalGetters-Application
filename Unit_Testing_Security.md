# Unit Testing Security Review — topic/testing

## Executive Summary

Tests are security-safe and deterministic. No secrets found and no real network/Firebase usage in unit tests. Coverage reporting is now enabled in Gradle and CI. Next, centralize coroutine test setup and consider unifying mocking.

## Scope

- Branch: `topic/testing`
- Unit tests: `app/src/test/**`
- Instrumented tests: `app/src/androidTest/**`
- Build config: `app/build.gradle.kts`
- CI: `.github/workflows/android-tests.yml`

## Tooling Detected

- JUnit 5 (Jupiter) with Platform engine, plus legacy JUnit 4
- Mocking: MockK, Mockito (core/inline)
- Coroutines test: `kotlinx-coroutines-test` with `StandardTestDispatcher`
- Robolectric (Android on JVM)
- Espresso (instrumented UI)
- JaCoCo enabled; `jacocoTestReport` generates XML/HTML and runs in CI

## Security Hygiene (Verified)

- No hardcoded secrets/API keys in test sources
- Unit tests mock repositories/services; no real network/Firebase usage
- Instrumented tests are Firestore-safe (graceful handling; do not hard‑fail)
- Coroutines tests set/reset `Dispatchers.Main`; use `advanceUntilIdle()`
- Android `Log` is mocked to prevent runtime issues
- CI runs tests on push/PR (main, staging, topic/**)

## Gaps / Risks

- Mixed mocking stacks (MockK + Mockito) increases maintenance overhead
- Coroutine dispatcher setup repeated across classes (not centralized)
- Some instrumented tests use sleeps (minor flakiness risk)

## Recommendations (Priority)

1) Centralize coroutine test setup
- Provide a JUnit 5 extension to set/reset `Dispatchers.Main`
- Share a `TestDispatcher` via the extension

2) Consolidate on one mocking framework (prefer MockK for Kotlin)

3) Reduce sleeps in instrumented tests
- Replace with Idling Resources or await conditions where feasible

4) (Optional) Add a minimum PR coverage threshold in CI

## Example snippets

```12:65:app/build.gradle.kts
plugins {
    // ... existing code ...
    jacoco
}

// --- JaCoCo Coverage
tasks.test {
    useJUnitPlatform()
    finalizedBy(tasks.jacocoTestReport)
}

tasks.register<JacocoReport>("jacocoTestReport") {
    dependsOn(tasks.test)
    reports {
        xml.required.set(true)
        html.required.set(true)
    }
    classDirectories.setFrom(
        fileTree("${buildDir}/tmp/kotlin-classes/debug") {
            exclude("**/R.class", "**/R$*.class", "**/BuildConfig.*", "**/Manifest*.*")
        }
    )
    sourceDirectories.setFrom(files("src/main/java", "src/main/kotlin"))
    executionData.setFrom(fileTree(buildDir) { include("**/jacoco/test*.exec", "**/jacoco/test*UnitTest.exec") })
}
```

```108:165:app/build.gradle.kts
dependencies {
    // ... existing app deps ...

    // Unit testing
    testImplementation(libs.junit) // JUnit 4 (legacy)
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.10.3")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.10.3")
}

// Ensure JUnit Platform runs
tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}
```

```1:95:.github/workflows/android-tests.yml
- name: Run Unit Tests + Coverage
  run: ./gradlew testDebugUnitTest jacocoTestReport

- name: Upload coverage report
  uses: actions/upload-artifact@v4
  with:
    name: coverage-report
    path: app/build/reports/jacoco/jacocoTestReport/html
```

## New Unit Tests Added

Locations:
- `app/src/test/java/com/ggetters/app/core/FinalTest.kt`
- `app/src/test/java/com/ggetters/app/ui/shared/extensions/MatchEventExtensionsTest.kt`
- `app/src/test/java/com/ggetters/app/core/InstantExtensionsTest.kt`
- `app/src/test/java/com/ggetters/app/core/ValidationBuilderTest.kt`
- `app/src/test/java/com/ggetters/app/core/StringExtensionsTest.kt`
- `app/src/test/java/com/ggetters/app/data/model/MatchDetailsTest.kt`
// Security-focused
- `app/src/test/java/com/ggetters/app/core/validation/PasswordSecurityValidationTest.kt`
- `app/src/test/java/com/ggetters/app/core/validation/WhitespaceExclusionValidationTest.kt`
- `app/src/test/java/com/ggetters/app/core/validation/EmailValidationTest.kt`

Impact:
- Expands baseline JVM coverage without Android/Firebase dependencies
- Verifies core result pattern, time conversions, and match helpers
- Guards credential paths: enforces password complexity, rejects whitespace and invalid emails

How to run:
- JVM unit tests: `./gradlew testDebugUnitTest` (or `./gradlew test`)
- With coverage: `./gradlew testDebugUnitTest jacocoTestReport`
- Coverage report: `app/build/reports/jacoco/jacocoTestReport/html/index.html`

## Conclusion

- Status: Security-safe tests and CI with coverage artifacts. No secret leakage or real-network usage in unit tests.
- Next: Centralize coroutine testing, consider unifying mocks, and optionally add PR coverage gates.

