# Learning Dashboard: Architecture & Design Notes

This document explains the design decisions behind the app in more depth than the one-page README. Everything described here matches the code in this repository.

**Stack:** Kotlin · Jetpack Compose (Material 3) · MVVM + Clean Architecture · Hilt · Room · Retrofit + kotlinx.serialization · DataStore · Coroutines/Flow · Navigation Compose (type-safe routes)
**SDK:** minSdk 26 · targetSdk 36 · compileSdk 37

---

## 1. The core rule

> **The UI observes local state. The repository decides how that state is updated.**

Room is the **single source of truth** for course data. The network never feeds the UI directly: it only refreshes Room, and every screen observes Room through `Flow`. Offline support, cross-screen consistency and survival across configuration changes all follow from this one rule.

---

## 2. Layers and dependency direction

```
presentation  ──►  domain  ◄──  data
                     ▲
                    di  (wires implementations to interfaces)
```

`domain` depends on nothing. It is pure Kotlin with no `android.*` imports, so its rules are tested with plain JUnit.

```
com.prajwalhs.learningdashboard
├── LearningDashboardApp.kt            @HiltAndroidApp
├── MainActivity.kt                    single Activity, hosts AppNavGraph
│
├── domain/
│   ├── model/        Course, Lesson, CourseDetail, AppResult, AppError, CredentialsValidation
│   ├── repository/   AuthRepository, CourseRepository            (interfaces)
│   ├── usecase/      ValidateCredentialsUseCase, LoginUseCase, HasActiveSessionUseCase,
│   │                 ObserveCoursesUseCase, RefreshCoursesUseCase,
│   │                 ObserveCourseDetailUseCase, MarkLessonCompletedUseCase
│   └── util/         ProgressCalculator
│
├── data/
│   ├── remote/       CourseApiService, dto/CourseDto, dto/LessonDto
│   ├── local/        LearningDatabase, dao/CourseDao,
│   │                 entity/CourseEntity, entity/LessonEntity,
│   │                 relation/CourseWithLessons, projection/CourseProgressProjection
│   ├── auth/         FakeAuthDataSource, SessionStore (DataStore)
│   ├── mapper/       CourseMapper, ErrorMapper
│   └── repository/   AuthRepositoryImpl, CourseRepositoryImpl
│
├── di/               NetworkModule, DatabaseModule, DataStoreModule, RepositoryModule
│
└── presentation/
    ├── navigation/   AppRoute, AppNavGraph, StartDestinationViewModel
    ├── components/   LoadingView, ErrorView, EmptyView, OfflineBanner, CourseProgressBar
    ├── util/         AppErrorText (AppError -> string resource)
    ├── theme/
    ├── login/        LoginScreen, LoginViewModel, LoginUiState
    ├── dashboard/    DashboardScreen, DashboardViewModel, DashboardUiState, CourseCard
    └── coursedetail/ CourseDetailScreen, CourseDetailViewModel, CourseDetailUiState, LessonRow
```

**Naming conventions:** `…Dto` (network), `…Entity` (database), no suffix (domain), `…UiState`, `…ViewModel`, `…UseCase`, `…Impl`. ViewModel event handlers are named `on…` (`onLoginClick`, `onRetryClick`, `onLessonCompleteClick`).

---

## 3. Presentation: unidirectional data flow

Each screen has:

- A **ViewModel** exposing a single `StateFlow<…UiState>`.
- A **stateful** composable (`LoginScreen`, `DashboardScreen`, `CourseDetailScreen`) that obtains the ViewModel and collects state with `collectAsStateWithLifecycle()`.
- A **stateless** composable (`LoginContent`, `DashboardContent`, `CourseDetailContent`) that renders state and reports events. It is previewable without Hilt.

```
State ──► *Content (render)
  ▲            │ user event
  │            ▼
ViewModel ◄── on…()
```

### Screen states

States are modelled **per screen**, not with a generic wrapper, because each screen has different cases:

| Screen | States |
|---|---|
| Login | a single `LoginUiState` data class: fields, field errors, `isLoading`, `loginError`, `isLoginSuccessful` |
| Dashboard | `Loading` · `Success(courses, isRefreshing, refreshError)` · `Empty` · `Error(AppError)` |
| Course Detail | `Loading` · `Success(CourseDetail)` · `NotFound` |

### How the dashboard decides what to show

`DashboardViewModel` combines two sources: the Room course list and a `RefreshStatus`. The decision lives in one pure function, `reduceDashboardState()`:

| Cached courses | Refresh status | Result |
|---|---|---|
| not empty | any | `Success`, with `refreshError` set if the last refresh failed |
| empty | running / not yet attempted | `Loading` |
| empty | failed | `Error` |
| empty | succeeded | `Empty` |

**Cached data always wins.** A full-screen error appears only when there is nothing to show. When cached data is shown after a failed refresh, the screen shows an `OfflineBanner` for `AppError.Network`, or an error banner for other failures.

Other presentation details:

- `stateIn(WhileSubscribed(5_000))`: upstream Room queries stop when the screen is in the background, but survive rotation.
- Overlapping refreshes are ignored, and the previous error is kept during a retry so the banner does not flicker.
- Login navigation is **modelled as state** (`isLoginSuccessful`), not a one-shot event channel, so it cannot be lost on configuration change.
- ViewModels expose `AppError`, never strings. `AppError.toUserMessage()` resolves text in Compose, which keeps ViewModels free of `Context`.

---

## 4. Data flow: marking a lesson complete

```
User taps "Mark complete"
        │
CourseDetailViewModel.onLessonCompleteClick(lessonId)
        │
MarkLessonCompletedUseCase → CourseRepository.markLessonCompleted()
        │
CourseDao: UPDATE lessons SET is_completed = 1 WHERE id = :lessonId
        │
Room invalidates the "lessons" table
        │
        ├──► observeCourseWithLessons(courseId)  → Course Detail recomposes (✓ + new %)
        └──► observeCourseProgress()             → Dashboard card already updated
```

No screen notifies another, and no result is passed back through navigation. The update is idempotent, so a double tap is harmless.

---

## 5. Offline-first design

### Refresh

`CourseRepositoryImpl.refreshCourses()`:

1. Calls `CourseApiService.getCourses()`.
2. Maps DTOs to `CourseEntity` / `LessonEntity`. The lesson's position in the response becomes `orderIndex`.
3. Calls `CourseDao.syncWithRemote(courses, lessons)`, a single `@Transaction`.
4. Returns `AppResult.Success`, or `AppResult.Failure(AppError)`. Room data is untouched on failure.

### `syncWithRemote`: one atomic transaction

1. Read the IDs of lessons completed locally.
2. Merge: a lesson stays completed if it is completed locally (**local completion wins**).
3. Delete courses no longer on the server. `ON DELETE CASCADE` removes their lessons.
4. Upsert courses.
5. Delete lessons no longer on the server, then upsert the merged lessons.

The UI never observes a half-written cache.

### Why local completion wins

```
Server: Lesson 114 → pending      (has not received the update)
Device: Lesson 114 → completed    (user completed it, possibly offline)
```

Blindly overwriting with server data would silently undo the user's progress. Until a real sync queue exists (see §11), local completion takes precedence. The rule is implemented as an interface default method on `CourseDao`, so it runs inside the transaction, and the unit tests' `FakeCourseDao` executes the same production logic.

### Why `@Upsert`, never `OnConflictStrategy.REPLACE`

`REPLACE` deletes the conflicting row and inserts a new one. With the `CASCADE` foreign key from `lessons.course_id`, replacing a course row would delete all of its lessons, including local progress. `@Upsert` updates in place.

---

## 6. Data model

### Tables

| Table | Columns |
|---|---|
| `courses` | `id` (PK, server ID), `title`, `instructor` |
| `lessons` | `id` (PK, server ID), `course_id` (FK → courses, CASCADE, indexed), `order_index`, `title`, `is_completed` |

The schema is exported to `app/schemas/` and committed, so future migrations can be verified. There is deliberately no `fallbackToDestructiveMigration()`, which would wipe local progress.

### Dashboard query: aggregation in SQL

```sql
SELECT c.id, c.title, c.instructor,
       COUNT(l.id)                        AS total_lessons,
       COALESCE(SUM(l.is_completed), 0)   AS completed_lessons
FROM courses c
LEFT JOIN lessons l ON l.course_id = c.id
GROUP BY c.id
ORDER BY c.id
```

It returns `CourseProgressProjection`, so the dashboard never loads full lesson lists just to count them.

### Progress is derived, never stored

```kotlin
ProgressCalculator.percent(completed, total)   // rounded half-up, 0 when total is 0, clamped to 0..100
```

`Course.progressPercent` is a computed property. Storing both a percentage and the lesson states would create two pieces of state that can disagree.

### API shape

`mock-api/courses.json` differs from the assignment's example in two deliberate ways:

- `lessons` is an **array** of `{ id, title, isCompleted }`, not a count. The detail screen needs the lessons, and one endpoint makes the detail screen work offline too.
- There is **no `progress` field**; it is derived.

The sample's "Generative AI, 40% of 16 lessons" equals 6.4 lessons, which is not possible. The data uses 6/16 = **38%**. The other courses match exactly (13/20 = 65%, 7/28 = 25%).

---

## 7. Error handling

Exceptions stop at the data layer. `ErrorMapper` translates them once:

| Exception | `AppError` |
|---|---|
| `IOException` (offline, timeout, DNS) | `Network` |
| `HttpException` | `Server(code)` |
| login rejected | `InvalidCredentials` |
| anything else (e.g. malformed JSON) | `Unknown` |

Repositories catch `CancellationException` first and **rethrow** it. A plain `catch (e: Exception)`, or `runCatching`, would swallow coroutine cancellation.

---

## 8. Navigation

- **Type-safe routes:** `AppRoute.Login`, `AppRoute.Dashboard`, `AppRoute.CourseDetail(courseId)` are `@Serializable` types. `CourseDetailViewModel` reads the argument with `SavedStateHandle.toRoute()`, so it survives process death.
- **Start destination:** `StartDestinationViewModel` checks for a stored session. `MainActivity` shows a loader until it is known, so Login never flashes for logged-in users.
- **Login → Dashboard** uses `popUpTo<AppRoute.Login> { inclusive = true }`, so Back from the Dashboard exits the app.
- **Double-tap safety:** `launchSingleTop` when opening a course; `dropUnlessResumed` on Back from the detail screen.

---

## 9. Dependency injection (Hilt)

| Module | Provides | Notes |
|---|---|---|
| `NetworkModule` | `Json`, `OkHttpClient`, `Retrofit`, `CourseApiService` | 15 s timeouts; body logging only in debug builds; `ignoreUnknownKeys` for forward compatibility |
| `DatabaseModule` | `LearningDatabase`, `CourseDao` | singleton database |
| `DataStoreModule` | `DataStore<Preferences>` | **must** be a singleton (two instances on one file throw); corruption handler resets instead of crashing |
| `RepositoryModule` | `@Binds` interface → implementation | presentation and domain only see interfaces |

Retrofit and Room suspend/Flow APIs are main-safe, so repositories do not switch dispatchers.

---

## 10. Testing

Tests check behaviour and decisions, not getters. They use hand-written fakes instead of a mocking library, so they assert outcomes and survive refactors.

| Test class | Cases |
|---|---|
| `ProgressCalculatorTest` (5) | exact percentage (13/20 = 65) · half-up rounding (6/16 = 38) · zero lessons → 0 · all completed → 100 · clamping bad data |
| `CourseRepositoryImplTest` (4) | **offline: refresh fails, cached courses still available** · local completion kept when the server says pending · courses removed on the server are deleted locally · HTTP 500 → `AppError.Server(500)` |
| `DashboardViewModelTest` (4) | first launch offline: `Loading` → `Error` · cached courses + network failure → `Success` with `refreshError` · successful empty refresh → `Empty` · Retry after an error → `Success` |

Supporting code: `MainDispatcherRule`, `FakeCourseApiService`, `FakeCourseDao` (reactive, emulates CASCADE), and `FakeCourseRepository` (with a `CompletableDeferred` gate to observe in-flight states deterministically).

```bash
./gradlew testDebugUnitTest
```

---

## 11. Security

### What the demo does

- The mock login (`FakeAuthDataSource`) returns a random token, stored in **plain DataStore** by `SessionStore`. It works offline.
- `android:allowBackup="false"`, so session data is not included in device backups.
- The password is never written to `SavedStateHandle`.
- HTTP body logging is disabled in release builds.

### What production would do

- **Access token:** short-lived and kept in memory.
- **Refresh token:** stored encrypted with a key held in the **Android Keystore** (DataStore + Tink).
- Token refresh through an OkHttp `Authenticator`.
- Certificate pinning (`CertificatePinner` in `NetworkModule`).
- R8 enabled for release builds (currently disabled to keep the assignment build simple).
- Clear the session and the database on logout.

---

## 12. Scaling to 1M users and hundreds of courses

1. **Paging 3 + `RemoteMediator`:** page courses from the API into Room instead of fetching everything at once.
2. **Progress sync:** a pending-changes table flushed by WorkManager with backoff, backed by an idempotent progress API. This replaces the "local completion wins" rule with real synchronization.
3. **Cheaper refreshes:** ETag / `If-None-Match` and delta sync (`updatedSince`); course media served from a CDN.
4. **Modularization:** `:feature:dashboard`, `:feature:coursedetail`, `:feature:login`, `:core:domain`, `:core:data`, `:core:database`, `:core:network`, `:core:designsystem`, for build speed and team ownership. Baseline Profiles for startup.
5. **Observability and safe releases:** crash reporting, performance monitoring, analytics, feature flags, staged rollouts.

The client architecture does not need to be redesigned for any of these; each fits into the existing layers.

---

## 13. Second platform

### Native iOS

| Android (this app) | iOS equivalent |
|---|---|
| Jetpack Compose | SwiftUI |
| ViewModel + `StateFlow` | `@Observable` view model |
| Coroutines / Flow | async/await, `AsyncSequence` |
| Retrofit + OkHttp | `URLSession` |
| Room (SSOT) | SwiftData (same SSOT pattern) |
| DataStore | `UserDefaults` for non-sensitive settings |
| Keystore-encrypted token | Keychain |
| Hilt | initializer injection / a DI container |
| Navigation Compose | `NavigationStack` |
| JUnit + fakes | XCTest + protocol-based fakes |

### Kotlin Multiplatform (at scale)

Because `domain` is pure Kotlin, `domain` and `data` could move into a shared KMP module while each platform keeps native UI:

```
        shared (Kotlin)
   domain · data · sync rules
        │             │
   Android         iOS
   Compose        SwiftUI
```

Required swaps: Retrofit → Ktor, Hilt → Koin. Room supports KMP and stays. The trade-off is Swift interop work (e.g. SKIE for `Flow`/`suspend`) and the iOS team adopting Kotlin tooling.

---

## 14. Key trade-offs

| Decision | Chosen | Alternative | Why |
|---|---|---|---|
| Source of truth | Room, observed via Flow | API responses held in ViewModel state | offline support, cross-screen consistency, survives process death |
| Progress | derived from lessons | stored percentage | no duplicated state that can disagree |
| Cache writes | `@Upsert` in one transaction | `REPLACE` / delete-all-then-insert | `REPLACE` + CASCADE would wipe local progress; partial writes never visible |
| Conflict rule | local completion wins | server always wins | server wins would silently undo user progress; replaced by a sync queue at scale |
| Aggregation | SQL `COUNT`/`SUM` projection | load all lessons and count in Kotlin | scales to large course catalogues |
| Errors | sealed `AppError` mapped in `data` | strings / exceptions across layers | typed, testable, localizable |
| Screen state | per-screen sealed states | one generic `UiState<T>` | screens have different cases (`Empty`, `NotFound`, refresh banners) |
| Test doubles | hand-written fakes | mocking library | assert outcomes, not calls; fake DAO runs the real merge logic |
| Mock API | static JSON fetched over the network | JSON bundled in assets | turning the internet off really exercises the offline path |

---

## 15. Known limitations (deliberate, given the limited scope)

- Mock authentication with a plain-DataStore token; no logout UI.
- No server-side progress sync or pending-changes queue.
- No pagination, no modularization, R8 disabled.
- The Empty state is covered by previews and unit tests; the demo data always has courses.

Each limitation has a defined path in §11 and §12. None requires changing the architecture.
