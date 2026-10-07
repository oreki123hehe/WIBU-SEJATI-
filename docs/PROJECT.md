# Wibu Sejati Project Contract

## UI
Home → Explore → Anime Detail → Episodes → Player.
Bottom navigation is Home / Explore / Watchlist / History.

## Data
Metadata comes from an API layer. Viewing-source data remains separate and optional.

## Automatic updates
Source/API → episode detector → state → notification adapter → deep link.

Deep link format:
wibusejati://episode?anime=<id>&episode=<number>

## Source boundary
No DRM bypass, protected-stream extraction, unauthorized re-hosting, or credential/session theft. A player URL must come from a source whose use/embedding is allowed.

## Backend
The backend exposes catalog/search/episode/event endpoints and includes a worker mode for detecting new episodes in followed anime.

## Next production step
Deploy backend, point the Android client at it, add a persistent database and an authorized notification provider, then build release APK/AAB.
