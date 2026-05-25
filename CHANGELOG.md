# CrashX v7.0.0

CrashX v7.0.0 is a major transparency, maintenance, and modernization release.

## Added

- Clear attribution to CustomActivityOnCrash and Eduard Ereza Martínez.
- NOTICE file with derivative-work note.
- Crash ID generation.
- Optional Crash ID display on the default crash screen.
- Optional CrashX version display on the default crash screen.
- Separate Restart and Close buttons.
- Configurable close button visibility.
- Configurable title, message, and all button text.
- Configurable primary and secondary button colors.
- Configurable title, message, meta, and button text colors.
- Optional crash report/share button.
- Optional support email.
- Optional report subject and chooser title.
- Optional additional report information.
- Optional stack trace inclusion.
- Optional device info inclusion.
- Optional build date inclusion.
- Optional activity lifecycle log inclusion.
- Configurable activity lifecycle log size.
- Configurable stack trace trimming size.
- Robust null-safe config extraction from intents.
- Robust CrashX installation with preserved previous uncaught exception handler.
- Improved background/foreground tracking.
- Improved restart loop protection.
- Library manifest with isolated error activity process.
- Modern sample MainActivity for testing all major features.

## Changed

- Version updated to v7.0.0.
- SharedPreferences file name changed to `crashx`.
- Default error UI redesigned for a cleaner production-ready look.
- Activity log is now a bounded queue.
- Crash reports include CrashX version, crash thread, crash ID, crash date, exception class, and optional metadata.
- Source headers now clearly mention the original project and CrashX maintenance.

## Fixed

- Safer handling when config is missing from the error activity intent.
- Safer provider initialization return value.
- Safer lifecycle activity counter handling.
- Safer fallback when no restart activity is found.
