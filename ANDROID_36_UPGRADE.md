# Android 35/36 Upgrade Guide

This document outlines the changes made to update the PermissionHandler library to support the latest Android versions, including Android 15 (API 35) and preparation for Android 36.

## Summary of Changes

### 1. SDK Version Updates

#### Build Configuration
- **compileSdk**: Updated from 34 to 35
- **targetSdk**: Updated from 34 to 35
- **Android Gradle Plugin**: Updated to 8.7.3 (when network access is available)
- **Kotlin**: Updated to 2.1.0
- **Gradle Wrapper**: Updated to 8.11.1

#### Updated Files:
- `PermissionHandler/build.gradle`: compileSdk and targetSdk updated to 35
- `app/build.gradle`: compileSdk and targetSdk updated to 35
- `build.gradle`: AGP version updated to 8.7.3, Kotlin to 2.1.0
- `gradle/wrapper/gradle-wrapper.properties`: Gradle updated to 8.11.1

### 2. Code Changes for Android Compatibility

#### Deprecated API Fixes

**Problem**: `PackageManager.getPermissionInfo(String, int)` with `GET_META_DATA` flag was deprecated in API 33+

**Solution**: Changed all calls from:
```kotlin
context.packageManager.getPermissionInfo(permission, GET_META_DATA)
```
to:
```kotlin
context.packageManager.getPermissionInfo(permission, 0)
```

**Files Modified**:
- `PermissionSingleHandler.kt`: 2 occurrences updated
- `PermissionMultiHandler.kt`: 2 occurrences updated

#### Android 14+ Photo Picker Support

**Problem**: Android 14 (API 34) introduced `READ_MEDIA_VISUAL_USER_SELECTED` permission for partial photo access

**Solution**: Added support for the new permission with proper version checks:

```kotlin
if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
    // Android 14+ supports partial access
    runWithPermissions(
        Manifest.permission.READ_MEDIA_IMAGES,
        Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED
    ) { ... }
} else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
    // Android 13
    runWithPermissions(Manifest.permission.READ_MEDIA_IMAGES) { ... }
} else {
    // Android 12 and below
    runWithPermissions(Manifest.permission.READ_EXTERNAL_STORAGE) { ... }
}
```

**Files Modified**:
- `MediaManager.kt`: 4 methods updated with Android 14+ support
  - `FragmentActivity.requestTakePhotoOrGallery()`
  - `Fragment.requestTakePhotoOrGallery()`
  - `FragmentActivity.requestPickupImageGallery()`
  - `Fragment.requestPickupImageGallery()`

#### Deprecated MediaStore API Fix

**Problem**: `MediaStore.Images.Media.getBitmap()` is deprecated

**Solution**: Updated to use `ImageDecoder` for API 28+ with proper fallback:

```kotlin
val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
    ImageDecoder.decodeBitmap(ImageDecoder.createSource(context.contentResolver, uri))
} else {
    @Suppress("DEPRECATION")
    MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
}
```

**Files Modified**:
- `MediaHandler.kt`: `copyImageToAppDir()` method updated

### 3. Dependency Updates

#### Library Module (`PermissionHandler/build.gradle`)
Current dependencies remain compatible:
- `androidx.appcompat:appcompat:1.7.0` (latest stable)
- `com.google.android.gms:play-services-location:21.3.0` (latest stable)
- `androidx.exifinterface:exifinterface:1.3.7` (latest stable)

#### App Module (`app/build.gradle`)
- `androidx.core:core-ktx:1.13.1` (updated from 1.9.0)
- `androidx.appcompat:appcompat:1.7.0` (updated from 1.5.1)
- `androidx.constraintlayout:constraintlayout:2.1.4` (kept stable version)
- `com.google.android.material:material:1.12.0` (updated from 1.7.0)

## Android 15 (API 35) Specific Changes

### Key Features Supported:
1. ✅ Photo picker with partial access (Android 14+)
2. ✅ Updated permission request APIs
3. ✅ Deprecated API replacements
4. ✅ Background location permission handling (Android 10+)
5. ✅ Scoped storage support (Android 11+)

### Backward Compatibility:
- Minimum SDK remains at 23 (Android 6.0)
- All version-specific code uses proper Build.VERSION checks
- Graceful fallbacks for older Android versions

## Android 36 Preparation

While Android 36 (API 36) SDK is available in the development environment, it's not officially released yet. The code is prepared to support API 36 with:

1. **Build Configuration**: Can easily update to compileSdk/targetSdk 36 when officially released
2. **Permission Handling**: Current implementation uses runtime permission checks compatible with future versions
3. **Deprecated APIs**: All known deprecated APIs have been replaced
4. **Version Checks**: All Android version checks use proper SDK_INT comparisons

### To Update to API 36 (when available):
1. Update `compileSdk` and `targetSdk` to 36 in both build.gradle files
2. Review Android 36 behavior changes documentation
3. Test permission flows on Android 36 devices
4. Update documentation with Android 36-specific requirements

## Testing Recommendations

### Before Release:
1. Test on Android 14+ devices for photo picker permissions
2. Verify permission dialogs on all supported Android versions (6.0 - 15)
3. Test background location permission flow (Android 10+)
4. Verify camera and gallery access on different Android versions
5. Test permission denial and settings navigation flows

### Build Requirements:
Due to network restrictions in the current environment, the build requires:
- Access to `google()` and `mavenCentral()` repositories
- Internet connection to download:
  - Android Gradle Plugin 8.7.3
  - Kotlin plugin 2.1.0
  - AndroidX dependencies
  - Google Play Services dependencies

## Known Issues

### Build Environment:
- Network access to `dl.google.com` is restricted in current environment
- AGP 8.7.3 and Kotlin 2.1.0 require download on first build
- All dependencies will be cached after successful first build

### Compatibility:
- No breaking changes introduced for existing API users
- All public APIs remain unchanged
- Behavior is enhanced for Android 14+ with backward compatibility

## Migration Guide for Users

### No Changes Required:
Users of this library do not need to make any changes. The library automatically:
- Detects Android version at runtime
- Requests appropriate permissions based on OS version
- Handles version-specific permission behaviors transparently

### Optional Enhancements:
Users targeting Android 14+ can benefit from:
- Partial photo access without full library permission
- Improved user experience with granular media selection
- Better privacy controls for end users

## References

- [Android 15 Behavior Changes](https://developer.android.com/about/versions/15/behavior-changes-15)
- [Photo Picker](https://developer.android.com/training/data-storage/shared/photopicker)
- [READ_MEDIA_VISUAL_USER_SELECTED Permission](https://developer.android.com/reference/android/Manifest.permission#READ_MEDIA_VISUAL_USER_SELECTED)
- [Deprecated APIs](https://developer.android.com/reference/android/provider/MediaStore.Images.Media#getBitmap(android.content.ContentResolver,%20android.net.Uri))

## Changelog

### Version 1.1 (Current)
- ✅ Updated to Android 15 (API 35)
- ✅ Added Android 14+ photo picker support with READ_MEDIA_VISUAL_USER_SELECTED
- ✅ Fixed deprecated PackageManager.getPermissionInfo() calls
- ✅ Fixed deprecated MediaStore.Images.Media.getBitmap()
- ✅ Updated build tools and dependencies
- ✅ Improved code documentation
- ✅ Maintained backward compatibility to Android 6.0

### Version 1.0 (Previous)
- Supported Android 14 (API 34)
- Basic permission handling
- Location and media permissions
