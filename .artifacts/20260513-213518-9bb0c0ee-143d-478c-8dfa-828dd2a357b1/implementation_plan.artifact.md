# Move Cloud Messaging to core:cloudmessaging

This plan outlines the steps to move the cloud messaging functionality from the `app` module to a new `core:cloudmessaging` module. This involves moving files, updating packages, and adjusting dependencies.

## Proposed Changes

### [core:cloudmessaging]

Create a new module for cloud messaging.

#### [build.gradle.kts](file:///C:/Users/georg/Dev/Escape/Escape-launcher/core/cloudmessaging/build.gradle.kts)

- Already exists, but ensure it has the correct plugins and namespace.

#### [NEW] [MessagingInitializer.kt](file:///C:/Users/georg/Dev/Escape/Escape-launcher/core/cloudmessaging/src/main/java/com/geecee/escapelauncher/core/cloudmessaging/MessagingInitializer.kt)

- Move from `app/src/main/java/com/geecee/escapelauncher/utils/MessagingInitializer.kt`
- Update package to `com.geecee.escapelauncher.core.cloudmessaging`

#### [NEW] [NotificationManager.kt](file:///C:/Users/georg/Dev/Escape/Escape-launcher/core/cloudmessaging/src/google/java/com/geecee/escapelauncher/core/cloudmessaging/NotificationManager.kt)

- Move from `app/src/google/java/com/geecee/escapelauncher/utils/managers/NotificationManager.kt`
- Update package to `com.geecee.escapelauncher.core.cloudmessaging`

#### [NEW] [MessagingInitializerImpl.kt (Google)](file:///C:/Users/georg/Dev/Escape/Escape-launcher/core/cloudmessaging/src/google/java/com/geecee/escapelauncher/core/cloudmessaging/MessagingInitializerImpl.kt)

- Move from `app/src/google/java/com/geecee/escapelauncher/utils/MessagingInitializerImpl.kt`
- Update package to `com.geecee.escapelauncher.core.cloudmessaging`
- Update import for `MessagingInitializer`

#### [NEW] [MessagingInitializerImpl.kt (FOSS)](file:///C:/Users/georg/Dev/Escape/Escape-launcher/core/cloudmessaging/src/foss/java/com/geecee/escapelauncher/core/cloudmessaging/MessagingInitializerImpl.kt)

- Move from `app/src/foss/java/com/geecee/escapelauncher/utils/MessagingInitializerImpl.kt`
- Update package to `com.geecee.escapelauncher.core.cloudmessaging`
- Update import for `MessagingInitializer`

---

### [app]

#### [EscapeApplication.kt](file:///C:/Users/georg/Dev/Escape/Escape-launcher/app/src/main/java/com/geecee/escapelauncher/EscapeApplication.kt)

- Update imports for `MessagingInitializerImpl` and `messagingInitializer`

#### [MainHomeScreenActivity.kt](file:///C:/Users/georg/Dev/Escape/Escape-launcher/app/src/main/java/com/geecee/escapelauncher/MainHomeScreenActivity.kt)

- Update import for `messagingInitializer`

#### [AndroidManifest.xml (Google)](file:///C:/Users/georg/Dev/Escape/Escape-launcher/app/src/google/AndroidManifest.xml)

- Update the name of `MessagingService` in the `<service>` tag to reflect the new package.

#### [build.gradle.kts](file:///C:/Users/georg/Dev/Escape/Escape-launcher/app/build.gradle.kts)

- Add `implementation(project(":core:cloudmessaging"))` to dependencies.

---

### Clean up

#### [DELETE] [MessagingInitializer.kt](file:///C:/Users/georg/Dev/Escape/Escape-launcher/app/src/main/java/com/geecee/escapelauncher/utils/MessagingInitializer.kt)
#### [DELETE] [NotificationManager.kt](file:///C:/Users/georg/Dev/Escape/Escape-launcher/app/src/google/java/com/geecee/escapelauncher/utils/managers/NotificationManager.kt)
#### [DELETE] [MessagingInitializerImpl.kt (Google)](file:///C:/Users/georg/Dev/Escape/Escape-launcher/app/src/google/java/com/geecee/escapelauncher/utils/MessagingInitializerImpl.kt)
#### [DELETE] [MessagingInitializerImpl.kt (FOSS)](file:///C:/Users/georg/Dev/Escape/Escape-launcher/app/src/foss/java/com/geecee/escapelauncher/utils/MessagingInitializerImpl.kt)

## Verification Plan

### Automated Tests
- Run `./gradlew assembleDebug` to ensure the project still builds correctly.

### Manual Verification
- Verify that the package names and imports are correctly updated in all moved files.
- Verify that the `app` module correctly depends on the new `core:cloudmessaging` module.
- Verify that the `AndroidManifest.xml` in the `google` flavor correctly points to the new location of `MessagingService`.
