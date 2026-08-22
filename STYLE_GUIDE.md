# Escape Launcher style guide.

> [!Warning]
> Yes, this file is AI generated. It just exists as a loose guide to make sure the code isn't too awful. I have read it through and try my best to follow it but it can be very strict in places so just keep in mind that it it is not absolute just try your best if your contributing..

> [!Warning]
> We are currently working on implenting this style in the refactor branch. The app still has a large amount of code that does not follow this guide.

Thank you for your interest in contributing! This guide covers code style, architecture conventions, and best practices used throughout the project. Following these keeps the codebase consistent and makes reviews easier for everyone.

## Table of contents

- [Project structure](#project-structure)
- [Module conventions](#module-conventions)
- [Kotlin style](#kotlin-style)
- [Jetpack Compose](#jetpack-compose)
- [Architecture](#architecture)
- [Naming](#naming)
- [File organisation](#file-organisation)
- [Build flavours](#build-flavours)
- [Testing](#testing)
- [Things to avoid](#things-to-avoid)

## Project structure

Escape Launcher uses a multi-module architecture split into three layers:

```
app/                        Entry point, wires everything together
core/
  common/                   Pure Android utilities, no UI (WorkProfileUtils, InstalledApp, etc.)
  ui/                       Shared composables, theme, colours, typography
  model/                    Shared data models
  data/                     Database and persistence
domain/
  time/                     Use cases (GetCurrentTimePartsUseCase, etc.)
feature/
  homescreen/               Clock ViewModel and feature-specific logic
  workapps/                 Work profile UI and ViewModel
  privatespace/             Private space UI and ViewModel
```

**The rule of thumb:** if something is purely Android API wrapping with no UI, it belongs in `core/common`. If it is a composable or theme value, it belongs in `core/ui`. If it is a screen or user-facing feature, it belongs in a `feature/` module. The `app` module should contain as little logic as possible — it wires modules together and hosts `MainActivity`. `domain` should be used for pure kotlin code that doesn't call any android APIs that is used in features. `core/domain` should be used for pure kotlin code that is reusable for the `core` module and not just one feature.

## Module conventions

Every module has its own `build.gradle.kts` using convention plugins defined in `build-logic/`. Do not add raw Gradle plugin IDs or dependency declarations that duplicate what a convention plugin already provides.

**Available convention plugins:**

| Plugin ID | What it does |
|---|---|
| `escapelauncher.android.library` | Base Android library config |
| `escapelauncher.android.application` | Base Android application config |
| `escapelauncher.android.compose` | Adds Compose BOM |
| `escapelauncher.android.composeui` | Compose + Material3 + icons |
| `escapelauncher.android.hilt` | Hilt DI setup |
| `escapelauncher.android.feature` | Convenience — applies library + compose.ui + hilt + testing |
| `escapelauncher.android.room` | Room database setup |
| `escapelauncher.android.testing` | JUnit + Espresso |
| `escapelauncher.android.flavours` | Google/FOSS product flavours |

New feature modules should use `escapelauncher.android.feature` unless there is a specific reason not to.

## Kotlin style

### General

Follow the [official Kotlin coding conventions](https://kotlinlang.org/docs/coding-conventions.html). The notes below are additions or clarifications specific to this project.

### Suppress annotations

Do not use `@Suppress` to silence warnings about unused variables or dead code. Remove the unused code instead. If something genuinely needs suppression (e.g. a lint rule that conflicts with an Android framework requirement), add a comment explaining why.

```kotlin
// Bad
@Suppress("AssignedValueIsNeverRead")
var showDialog by remember { mutableStateOf(false) }

// Good — just remove it if it's unused, or use it if it's needed
```

### lateinit globals

Avoid `lateinit var` at the top level for dependency injection. The proxy objects (`analyticsProxy`, `weatherProxy`, `messagingInitializer`) currently exist for flavour compatibility and are an accepted exception. Do not add new ones — use Hilt constructor injection instead.

### Coroutines

Prefer `viewModelScope.launch` inside ViewModels. Use `Dispatchers.IO` for database and network work, and always switch back to `Dispatchers.Main` before updating UI state.

```kotlin
viewModelScope.launch(Dispatchers.IO) {
    val result = repository.getData()
    withContext(Dispatchers.Main) {
        _state.value = result
    }
}
```

### Extension functions

Put extension functions close to where they are used. If an extension is used in more than one module, move it to `core/common`.

## Jetpack Compose

### Composable size and responsibility

A composable should do one thing. If it is longer than roughly 150 lines, it is doing too much. Extract child composables.

```kotlin
// Bad — one giant composable handling its own search logic, list, and bottom sheet
@Composable
fun AppsList(mainAppModel: MainAppModel, homeScreenModel: HomeScreenModel) {
    // 300 lines...
}

// Good — delegate to focused child composables
@Composable
fun AppsList(apps: List<InstalledApp>) {
    Box(Modifier.fillMaxSize()) {
        AppsLazyColumn(apps, Modifier)
        AppsBottomSearchBar()
    }
}
```

### Local composable functions

Do not define composables as local functions inside another composable. Define them as private top-level functions in the same file instead.

```kotlin
// Bad
@Composable
fun AppsList(...) {
    @Composable
    fun SearchBox() { ... } // local composable — don't do this
}

// Good
@Composable
private fun SearchBox(...) { ... }

@Composable
fun AppsList(...) {
    SearchBox(...)
}
```

### State

Prefer `rememberSaveable` over `remember` for UI state that should survive configuration changes (e.g. whether a dialog is open, the value of a text field).

```kotlin
// Prefer this for dialog/sheet visibility and user-entered text
var showDialog by rememberSaveable { mutableStateOf(false) }

// Use plain remember only for objects that cannot be saved (e.g. LazyListState)
val scrollState = rememberLazyListState()
```

### Modifier parameter

All composables that render content should accept a `modifier: Modifier = Modifier` parameter as their first parameter after required data parameters. Pass it to the outermost layout element.

```kotlin
@Composable
fun HomeScreenItem(
    appName: String,
    modifier: Modifier = Modifier,
    onAppClick: () -> Unit,
) {
    Text(appName, modifier = modifier)
}
```

### Preview

Add a `@Preview` annotated composable for any composable that renders standalone UI. Keep previews at the bottom of the file.

---

## Architecture

Escape Launcher follows a standard MVVM pattern with Hilt for dependency injection.

### ViewModels

- One ViewModel per screen or feature, not one per file.
- ViewModels live in the `feature/` module they belong to, not in `app/`.
- Expose state as `StateFlow` or Compose `State`, not raw mutable fields.
- Never hold a reference to a `Context` in a ViewModel unless it is an `ApplicationContext` injected via `@ApplicationContext`. Never hold an `Activity` or `View`.

```kotlin
// Bad — ViewModel knowing about UI lifecycle
class MyViewModel(private val activity: Activity) : ViewModel()

// Good
@HiltViewModel
class MyViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel()
```

### Separating UI from logic

Composables should not contain business logic. They observe state and emit events upward.

```kotlin
// Bad — composable talking directly to the manager
@Composable
fun HiddenAppsScreen(mainAppModel: MainAppModel) {
    Button(onClick = {
        mainAppModel.hiddenAppsManager.removeHiddenApp(packageName) // business logic in UI
    }) { ... }
}

// Good — composable delegates upward through the ViewModel
@Composable
fun HiddenAppsScreen(viewModel: HiddenAppsViewModel = hiltViewModel()) {
    Button(onClick = { viewModel.unhideApp(packageName) }) { ... }
}
```

## Naming

### Files

- One primary public declaration per file. The file name matches the declaration name.
- Do not name a file after a collection of loosely related things (`HomeScreenComposables.kt`, `AppUtils.kt`) — use a name that describes the single responsibility.
- Utility files that genuinely contain a single coherent group of related functions are acceptable in `core/common` (e.g. `WorkProfileUtils.kt`), but keep them focused.

### Classes and objects

| Thing | Convention | Example |
|---|---|---|
| ViewModel | `<Feature>ViewModel` | `WorkAppsViewModel` |
| Composable file | `<Feature>.kt` | `WorkApps.kt` |
| Manager | `<Resource>Manager` | `FavoriteAppsManager` |
| Use case | `<Verb><Noun>UseCase` | `GetCurrentTimePartsUseCase` |
| Interface | Descriptive noun | `AnalyticsProxy`, `WeatherProxy` |

### Functions

- Composables use `PascalCase`.
- Regular functions use `camelCase`.
- Boolean returning functions are prefixed with `is`, `has`, `can`, or `does` (`isAppHidden`, `doesPrivateSpaceExist`).

### Do not duplicate

If a utility function already exists somewhere in the codebase, do not create a second copy. If the existing copy is in the wrong module for your use case, move it to `core/common` and update the import.

## File organisation

### Within a file

Order declarations as follows:

1. Top-level constants and type aliases
2. Public composables (largest / entry point first)
3. Private composables
4. Preview composables (annotated with `@Preview`)

### Within a feature module

```
feature/myfeature/
  src/main/java/.../feature/myfeature/
    MyFeatureViewModel.kt   ← ViewModel
    MyFeature.kt            ← Top-level composable(s)
    MyFeatureComponents.kt  ← Private sub-composables (if the file gets large)
```

Do not put composables and ViewModels in the same file.

## Build flavours

The project has two product flavours: `google` and `foss`.

- Features that require Google Play Services, Firebase, or proprietary APIs must be gated behind the `google` flavour source set (`src/google/`).
- The `foss` flavour source set (`src/foss/`) must provide a no-op implementation of every interface the `google` source set implements.
- Use `BuildConfig.IS_FOSS` in shared code to branch at runtime only when a compile-time source set split is not possible.
- Do not add Google dependencies to `build.gradle.kts` directly — add them to `google.gradle.kts` using the flavour-aware `googleImplementation` configuration.

```kotlin
// Bad — feature only available on one flavour, no guard
Firebase.analytics.logEvent("app_opened", null)

// Good — gated or abstracted through a proxy
analyticsProxy.logCustomKey("event", "app_opened")
```

## Testing

- Unit tests go in `src/test/`, instrumented tests in `src/androidTest/`.
- Test files are named `<ClassUnderTest>Test.kt`.
- Every new use case class in `domain/` should have a corresponding unit test.
- ViewModels should be tested with a `TestCoroutineDispatcher` and fake/mock dependencies rather than real implementations.

## Things to avoid

**Mixed concerns in one file.** A file containing a `@Composable` function, a manager class, and a data class is a sign that responsibility boundaries are unclear. Split them.

**Composables inside manager files.** The `OpenChallenge` composable living in `OpenChallengesManager.kt` is an existing example of this — new code should not repeat the pattern. UI goes in the `feature/` or `core/ui` module; business logic goes in managers or use cases.

**Direct SharedPreferences access in composables.** Composables should read state from a ViewModel, not call `getBooleanSetting(context, ...)` themselves. The current codebase does this in several places as a legacy pattern — do not extend it.

**Suppressing instead of fixing.** If the compiler or lint is warning you about something, treat it as useful information. Remove unused code, fix nullability issues, and resolve deprecation warnings rather than suppressing them.

**Giant single-responsibility violations.** `Settings.kt` is the main example. When adding features, resist the pull toward the nearest large file — create a new one instead.
