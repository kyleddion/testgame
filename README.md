# Flappy Bird Clone

This repository contains a minimal Flappy Bird style game implemented for Android.
It can be opened with Android Studio.

## Building

1. Open the project in Android Studio.
2. Let Android Studio download the required dependencies.
3. Run the app on an emulator or physical device.

Note: Gradle may require internet access on the first build to download the
Android Gradle Plugin and other dependencies.

This project targets Android 12 (API level 31) and higher, so the
`android:exported` attribute is declared on `MainActivity` in the manifest to
avoid manifest merger errors.
