# Mosquito Hunter

A portrait Android arcade game written in native Java. Rendering uses a `SurfaceView`
with a dedicated 60 FPS thread, Android Canvas, delta-time movement, pooled entities,
`SharedPreferences` for scores and settings, and `SoundPool` + `MediaPlayer` for audio.

## Requirements
- Android Studio Hedgehog (2023.1) or newer
- JDK 17 (bundled with recent Android Studio)
- Android SDK Platform 34
- Minimum device: Android 7.0 (API 24)

## Build and run
1. Unzip `MosquitoHunter.zip` and open the `MosquitoHunter` folder in Android Studio.
2. Let Gradle sync. If Studio reports a missing Gradle wrapper JAR, run
   `gradle wrapper --gradle-version 8.7` once in the project folder (or use
   File > Settings > Build Tools > Gradle and let Studio regenerate it).
3. Choose a device or emulator and press Run (Shift+F10).
4. For a release APK: Build > Build Bundle(s) / APK(s) > Build APK(s).

## Regenerating sound assets
The WAV files in `app/src/main/res/raw/` are synthesized originals produced by
`tools/generate_sounds.py`. They can be regenerated with `python3 tools/generate_sounds.py`.

## Gameplay summary
- Tap and drag to aim; release to fire a laser.
- Mosquito types: Normal (10), Fast (20), Golden (50), Giant (30, two hits), Tiny (25).
- Waves grow from 2 mosquitoes (wave 1) by 2 per wave, capped at 20.
- Three lives. A mosquito that is not killed in time escapes and costs one life.
- Combos multiply points up to x5 and reset after a missed volley or a timeout.
- Every 12 kills grants a random power-up charge: SLOW, MULTI, FREEZE or BOOST.
- Settings: sound, music, vibration, particles, difficulty (Easy / Normal / Hard).
- Best score, best wave and lifetime stats persist across launches.

## Permissions
Only `VIBRATE` is declared. No network access is used.
