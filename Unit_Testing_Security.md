# Unit Testing Security Review — topic/testing

## Executive Summary

Tests are security-safe and deterministic. No secrets found and no real network/Firebase usage in unit tests. Minor improvements recommended for coverage reporting and centralized coroutine test setup.

## Scope

- Branch: `topic/testing`
- Unit tests: `app/src/test/**`
- Instrumented tests: `app/src/androidTest/**`
- Build config: `app/build.gradle.kts`
- CI: `.github/workflows/android-tests.yml`

## Tooling Detected

- JUnit 5 (Jupiter), Kotlin Test
- Mocking: MockK, Mockito (core/inline)
- Coroutines test: `kotlinx-coroutines-test` with `StandardTestDispatcher`
- Robolectric (Android on JVM)
- Espresso (instrumented UI)
- JaCoCo dependency present (coverage not yet wired in CI)

## Security Hygiene (Verified)

- No hardcoded secrets/API keys in test sources
- Unit tests mock repositories/services; no real network/Firebase usage
- Instrumented tests are Firestore-safe (graceful handling; do not hard‑fail)
- Coroutines tests set/reset `Dispatchers.Main`; use `advanceUntilIdle()`
- Android `Log` is mocked to prevent runtime issues
- CI runs tests on push/PR (main, staging, topic/**)

## Gaps / Risks

- Coverage not reported/gated in CI despite JaCoCo dependency
- Mixed mocking stacks (MockK + Mockito) increases maintenance overhead
- Coroutine dispatcher setup repeated across classes (not centralized)
- Some instrumented tests use sleeps (minor flakiness risk)

## Recommendations (Priority)

1) Enable coverage reporting and artifact upload
- Add a `jacocoTestReport` task and run it in CI
- Optionally set a minimum PR coverage threshold

2) Centralize coroutine test setup
- Provide a JUnit 5 extension to set/reset `Dispatchers.Main`
- Share a `TestDispatcher` via the extension

3) Consolidate on one mocking framework (prefer MockK for Kotlin)

4) Reduce sleeps in instrumented tests
- Replace with Idling Resources or await conditions where feasible

## Example snippets

```kotlin
// app/build.gradle.kts — JaCoCo
plugins { jacoco }

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
            exclude(
                "**/R.class", "**/R$*.class", "**/BuildConfig.*",
                "**/Manifest*.*", "**/*$ViewInjector*.*", "**/*$ViewBinder*.*"
            )
        }
    )
    sourceDirectories.setFrom(files("src/main/java", "src/main/kotlin"))
    executionData.setFrom(fileTree(buildDir) { include("**/jacoco/test*.exec", "**/jacoco/test*UnitTest.exec") })
}
```

```yaml
# .github/workflows/android-tests.yml — upload coverage
- name: Run Unit Tests + Coverage
  run: ./gradlew testDebugUnitTest jacocoTestReport

- name: Upload coverage report
  uses: actions/upload-artifact@v4
  with:
    name: coverage-report
    path: app/build/reports/jacoco/jacocoTestReport/html
```

## New Security Tests Added

Location: `app/src/test/java/com/ggetters/app/security/`

- AuthValidatorTest.kt: Enforces password complexity (reject weak, accept strong)
- SignInViewModelSecurityTest.kt: Invalid inputs yield Failure; no AuthService call
- SignUpViewModelSecurityTest.kt: Weak/mismatched passwords fail fast; no AuthService call
- AuthServiceSecurityTest.kt: `isUserSignedIn` correctness; `logout` triggers `signOut()`
- ForgotPasswordViewModelSecurityTest.kt: Invalid email fails fast; no service call

Impact:
- Blocks risky auth paths before backend interaction
- Verifies logout/security flows
- Improves guardrail coverage of credential validation

How to run:
- JVM unit tests: `./gradlew testDebugUnitTest` (or `./gradlew test`)

## Conclusion

- Status: Security-safe tests and CI. No secret leakage or real-network usage in unit tests.
- Next: Enable coverage in CI, centralize coroutine testing, consider unifying mocks.

