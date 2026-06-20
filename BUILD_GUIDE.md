# Profile Finder — Complete Build Guide

## What's Included

```
ProfileFinder/
├── app/
│   ├── build.gradle.kts              # App-level Gradle config
│   ├── proguard-rules.pro            # Release obfuscation rules
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── java/com/profilefinder/
│       │   ├── MainActivity.kt
│       │   ├── ProfileFinderApp.kt   # Hilt Application class
│       │   ├── data/
│       │   │   ├── api/ApiInterfaces.kt       # GitHub + Reddit Retrofit APIs
│       │   │   ├── db/Database.kt             # Room database + DAOs
│       │   │   ├── models/Models.kt           # All data models
│       │   │   └── repository/
│       │   │       ├── ProfileRepository.kt
│       │   │       └── ImageAnalysisRepository.kt
│       │   ├── di/AppModule.kt       # Hilt dependency injection
│       │   └── ui/
│       │       ├── ViewModels.kt
│       │       ├── Navigation.kt
│       │       ├── theme/Theme.kt
│       │       └── screens/
│       │           ├── HomeScreen.kt
│       │           ├── ImageAnalysisScreen.kt
│       │           ├── ProfileSearchScreen.kt
│       │           └── HistoryScreen.kt
│       └── res/
│           ├── values/strings.xml
│           ├── values/themes.xml
│           └── xml/
│               ├── network_security_config.xml
│               ├── file_paths.xml
│               ├── backup_rules.xml
│               └── data_extraction_rules.xml
├── gradle/
│   ├── libs.versions.toml            # Version catalog
│   └── wrapper/gradle-wrapper.properties
├── .github/workflows/build.yml       # GitHub Actions CI — auto builds APK
├── build.gradle.kts                  # Root Gradle
├── settings.gradle.kts
└── .gitignore
```

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Kotlin |
| UI | Jetpack Compose + Material 3 |
| DI | Hilt |
| Database | Room |
| Networking | Retrofit 2 + OkHttp |
| Image Loading | Coil |
| AI / ML | ML Kit (Text Recognition + Object Detection) |
| Camera | CameraX |
| Navigation | Jetpack Navigation Compose |
| Permissions | Accompanist Permissions |
| Async | Kotlin Coroutines + StateFlow |

---

## Option 1 — Build APK via GitHub Actions (No local setup needed)

This is the **easiest method**. GitHub builds the APK in the cloud for free.

### Steps

1. **Create a GitHub account** at https://github.com if you don't have one

2. **Create a new repository**
   - Go to https://github.com/new
   - Name it `ProfileFinder`
   - Set it to **Public** or **Private**
   - Click "Create repository"

3. **Upload the project files**

   **Option A — GitHub website (drag & drop):**
   - Open your new repo
   - Click "uploading an existing file"
   - Drag and drop the entire `ProfileFinder` folder contents
   - Click "Commit changes"

   **Option B — Git command line:**
   ```bash
   cd ProfileFinder
   git init
   git add .
   git commit -m "Initial commit"
   git branch -M main
   git remote add origin https://github.com/YOUR_USERNAME/ProfileFinder.git
   git push -u origin main
   ```

4. **Wait for the build**
   - Go to your repo → click the **Actions** tab
   - You'll see "Build APK" workflow running (takes ~5–10 minutes)
   - Wait for the green checkmark ✅

5. **Download the APK**
   - Click on the completed workflow run
   - Scroll down to **Artifacts**
   - Click **ProfileFinder-debug-APK** to download
   - Unzip the downloaded file → you get `app-debug.apk`

6. **Install on your Android phone**
   - Transfer `app-debug.apk` to your phone (email, USB, Google Drive)
   - On your phone: Settings → Security → Enable "Install from Unknown Sources"
   - Open the APK file on your phone → tap Install

---

## Option 2 — Build APK via Android Studio (Local)

### Prerequisites

| Tool | Download |
|---|---|
| Android Studio | https://developer.android.com/studio |
| JDK 17+ | Bundled with Android Studio |
| Android SDK | Installed via Android Studio |

### Steps

1. **Install Android Studio**
   - Download from https://developer.android.com/studio
   - Run the installer and follow prompts
   - Let it install the Android SDK (default location is fine)

2. **Open the project**
   - Launch Android Studio
   - Click "Open" → select the `ProfileFinder` folder
   - Wait for Gradle sync to complete (first time takes 3–10 minutes, downloads dependencies)

3. **Build the Debug APK**
   - Menu: **Build → Build Bundle(s)/APK(s) → Build APK(s)**
   - Wait for build to finish (~2–5 minutes)
   - Click **"locate"** in the notification, or find it at:
     ```
     app/build/outputs/apk/debug/app-debug.apk
     ```

4. **Install on device**

   **Method A — Direct USB install:**
   - Enable Developer Options on your phone (Settings → About → tap "Build Number" 7 times)
   - Enable USB Debugging (Settings → Developer Options → USB Debugging)
   - Connect phone via USB
   - In Android Studio: click the **Run ▶** button
   - Select your device → app installs and launches

   **Method B — Copy APK:**
   - Copy `app-debug.apk` to your phone
   - Tap the file to install (may need to enable "Install unknown apps" for your file manager)

---

## Option 3 — Command Line Build (Advanced)

```bash
# Requires: JDK 17, Android SDK, ANDROID_HOME set

cd ProfileFinder
chmod +x gradlew

# Debug APK
./gradlew assembleDebug

# APK location:
# app/build/outputs/apk/debug/app-debug.apk

# Release APK (unsigned)
./gradlew assembleRelease
# app/build/outputs/apk/release/app-release-unsigned.apk
```

---

## How to Sign a Release APK (for distribution)

For production/Play Store, create a keystore:

```bash
keytool -genkey -v \
  -keystore profilefinder.keystore \
  -alias profilefinder \
  -keyalg RSA \
  -keysize 2048 \
  -validity 10000
```

Then add to `app/build.gradle.kts`:

```kotlin
android {
    signingConfigs {
        create("release") {
            storeFile = file("../profilefinder.keystore")
            storePassword = "YOUR_STORE_PASSWORD"
            keyAlias = "profilefinder"
            keyPassword = "YOUR_KEY_PASSWORD"
        }
    }
    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("release")
        }
    }
}
```

---

## Features Overview

### Screen 1 — Home
- App landing page with feature cards
- Navigate to Image Analysis or Profile Search
- Privacy notice clearly displayed
- Supported platforms shown (GitHub, Reddit, Website URLs)

### Screen 2 — Image Analysis
- Upload photo from gallery OR take photo with camera
- AI analysis with animated progress bar (30–90 second simulation)
- Results show:
  - Object detection (labels + confidence %)
  - OCR / text extraction
  - Image description
  - Overall confidence score

### Screen 3 — Profile Search
- Tab switcher: Username | URL / Website
- Username search → queries GitHub API + Reddit API simultaneously
- URL search → auto-detects platform (GitHub, Reddit, generic)
- Results show:
  - Avatar, display name, username
  - Public bio
  - Public stats (repos, followers, karma, etc.)
  - Verified links
  - Source attribution
  - "Open Profile" button → opens in browser

### Screen 4 — History
- All past searches with timestamps
- Query type badge (USERNAME / URL)
- Result summary per search
- Clear all history option

---

## Privacy & Security

- **No face recognition** — ML Kit object detection only, no facial biometrics
- **No private data** — only GitHub Public API and Reddit Public API are queried
- **No phone numbers or emails** extracted
- **Network security config** — HTTPS only, no cleartext traffic
- **Data stays on device** — Room database is local only
- **ProGuard** — code is obfuscated in release builds
- **No ads** — no tracking SDKs included

---

## Troubleshooting

| Problem | Solution |
|---|---|
| Gradle sync fails | File → Invalidate Caches → Restart |
| "SDK not found" | Open SDK Manager, install Android 14 (API 35) |
| Build fails first time | Wait for full Gradle dependency download (~500MB) |
| APK won't install | Enable "Install from Unknown Sources" in phone settings |
| Camera not working | Grant camera permission when prompted in app |
| ML Kit error | Internet required for first-time model download |
| GitHub Actions fails | Check Actions tab → click failed job → read logs |

---

## Minimum Requirements

- **Android version:** 8.0 Oreo (API 26) or higher
- **RAM:** 2 GB+
- **Storage:** ~50 MB for app + ML models
- **Internet:** Required for profile search (not required for image analysis after first launch)
