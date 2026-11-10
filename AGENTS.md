# AGENTS.md

Rules for any coding agent or assistant working in this repository. Update this file when an agent gets something wrong.

## Project

SplitShare: an Android app (Kotlin, Jetpack Compose) for shared expenses in groups.
Graded quality attribute: **reliability** (fault tolerance, offline availability, user feedback).

## Build setup

- Created from Android Studio's "Empty Activity" template. Keep that structure: package `com.example.splitshare`, theme in `ui/theme`.
- AGP 9 has Kotlin built in. Do not add the `org.jetbrains.kotlin.android` plugin.
- Library versions live only in `gradle/libs.versions.toml`.
- minSdk 24, Java target 11. Gradle runs on the JDK set in `gradle/gradle-daemon-jvm.properties`.

## Workflow

- `master` is protected. All changes go through a pull request.
- One branch per user story, named after it, e.g. `us6-add-expense`.
- The PR names the user stories and requirements it covers (US#, NFR#, S#) and fills in the PR template.

## Architecture

New code goes into these packages under `com.example.splitshare`. Dependencies point downwards only:

- `ui` (screens, navigation, theme) → `viewmodel` → `domain` and `data`
- `domain` has no Android imports, so it can be unit tested on the JVM.
- `ui` never touches the database, the API, or WorkManager directly.

Navigation follows the lectures: an `object Routes` with route constants and an `AppNavigation` composable holding the `NavHost`. Screens receive callbacks instead of the `NavController`.

Reliability design (do not bypass):

- **Local-first writes:** every write is stored locally first, marked `PENDING`, then queued for sync.
- **Background retry:** WorkManager sends queued requests with a network constraint and exponential backoff.
- **Idempotent requests:** ids are client-generated UUIDs, so retries never duplicate data.
- **Feedback:** synced items expose a status (`PENDING`, `SYNCED`, `FAILED`). A notification is shown when a queued request succeeds.
- **Offline reads:** screens read local data, never the network directly.

## Conventions

- Money is a `Long` in minor units (øre). Never `Double`.
- Screen state is a `UiState` data class exposed as `StateFlow` from the ViewModel.
- Screens map to the navigation model's S-IDs (S1–S10).

## Definition of done

- Builds, and all tests pass in CI.
- New logic has tests.
- Reviewed by a teammate who did not write or generate the code.
- AI-assisted work is disclosed in the PR and logged in the design document's Appendix A.

## Known agent mistakes

Add an entry each time an agent's output was wrong, and what the rule is now.

- An agent generated a project with outdated library versions and the separate Kotlin Android plugin. Rule: use the versions and plugins in this repo's version catalog; AGP 9 needs no Kotlin plugin.
