# Cycle Tracker (Starter)

This is a **starter** Android app for tracking menstruation cycles. It is **offline-only**, uses **local encrypted storage**, and includes functional UI for logging, calendar, predictions, and reminders.

## Features (MVP scaffolding)
- Cycle calendar with period/ovulation highlights
- Period start/end logging (stored locally) with **delete support**
- Ovulation window prediction
- Predictions for next cycle
- Daily reminders via WorkManager
- Offline-only data with encrypted Room database
- **Dark mode** support (follows system preference)
- **Navigation icons** in the bottom bar for intuitive tab switching

## Recent improvements
- Dark mode: `CycleTheme` now respects the system dark/light preference
- Delete period logs: tap the trash icon next to any log entry to remove it
- Navigation bar icons: each tab now displays a meaningful Material icon
- Safer Room setup: `fallbackToDestructiveMigration()` prevents crashes on schema changes
- Code quality: replaced deprecated `DayOfWeek.values()` with `DayOfWeek.entries`; explicit `5L` literal in period-length fallback; clearer `require()` error message in log input validation

## Next steps
- Add richer calendar styling and analytics
- Improve notification messaging with symptom tracking
- Add backups/export (still offline) if needed

## Build
Open the project root in Android Studio and run the `app` configuration.
