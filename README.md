# Learning Dashboard (Android)

Kotlin · Jetpack Compose · MVVM + Clean Architecture · Hilt · Room · Retrofit · Coroutines/Flow

**Demo login:** `student@demo.com` / `password123` · **APK:** <link> · **Demo video:** <link>
**Run:** open in Android Studio, run `app`. **Tests:** `./gradlew testDebugUnitTest`

The mock API is a static JSON file in this repo (`mock-api/courses.json`), fetched over the network via `raw.githubusercontent.com`, so offline behaviour can be tested for real by turning the internet off.

## 1. Architecture
```
UI (Compose) → ViewModel (StateFlow<UiState>) → UseCase → Repository (interface, domain)
                                                              ↓
                                          RepositoryImpl → Retrofit API + Room
```
- **Layers:** `domain` is pure Kotlin (models, repository contracts, use cases), so business rules are unit-tested without Android. `data` owns DTOs/entities/mappers; `presentation` owns UI state.
- **Unidirectional data flow:** each screen exposes one sealed `UiState`; composables are stateless (`*Content`) and previewable.
- **Room is the single source of truth.** Marking a lesson complete is a single DB write; the detail screen and the dashboard update automatically through Flows, with no manual cross-screen state passing.
- **Errors** are mapped once in `data` to a sealed `AppError`; the UI maps them to string resources.

## 2. Offline support
- The UI only **observes Room**. `refreshCourses()` fetches the API and writes to Room in one `@Transaction`.
- If a refresh fails: cached courses are shown with an offline banner. A full-screen error appears **only** when the cache is empty.
- Lesson completion is written locally and works offline. On the next refresh, **local completion wins** over server state, since the server has not received the update yet.
- `@Upsert` instead of `REPLACE`: REPLACE deletes rows, and the `CASCADE` foreign key would wipe local progress.

## 3. Security (token storage in production)
- The access token is **short-lived and kept in memory**; the refresh token is stored **encrypted with an Android Keystore key** (DataStore + Tink), never in plain SharedPreferences.
- Refresh via an OkHttp `Authenticator`; certificate pinning; R8 obfuscation; `allowBackup=false`; clear the session and database on logout.
- *This demo stores a mock token in plain DataStore; see trade-offs.*

## 4. Scale (1M users, hundreds of courses)
1. **Paging 3 + `RemoteMediator`**: page courses from the API into Room instead of loading everything at once.
2. **Background sync of progress**: a pending-changes queue flushed by WorkManager, backed by an idempotent progress API.
3. **Cheaper refreshes**: ETag / `If-None-Match` and delta sync (`updatedSince`); course content served from a CDN.
4. **Feature modularization** (`:feature:*`, `:core:*`) for build speed and team ownership, plus Baseline Profiles for startup.
5. **Observability and safe releases**: Crashlytics, performance and analytics, feature flags, and staged rollouts.

## 5. Second platform (iOS)
**Native:** SwiftUI views with `@Observable` ViewModels; a repository behind a protocol; `URLSession` with async/await; SwiftData as the cache (same single-source-of-truth pattern); Keychain for tokens; `NavigationStack`; XCTest.
**At scale, Kotlin Multiplatform:** since `domain` is pure Kotlin, move domain and data into a shared module (Retrofit → Ktor, Hilt → Koin, Room KMP stays), keeping native UI on both platforms. The trade-off is Swift interop work (e.g. SKIE for Flow) and the iOS team adopting Kotlin tooling.

## Deliberate trade-offs (3-hour scope)
- **JSON shape:** `lessons` is an array (the details screen needs it) and progress is **derived** from lesson completion, not stored. The sample's "40% of 16 lessons" is not a whole lesson count (6.4), so the data uses 6/16 = **38%**.
- **Mock login** (works offline), plain-DataStore token, no logout screen, no server sync queue.
- **Tests** cover the progress rules, repository offline fallback and merge rule, and dashboard state transitions, using hand-written fakes.