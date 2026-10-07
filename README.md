# WIBU SEJATI

Android anime catalog/player app in Indonesian.

## Included in this milestone
- Android app project
- Home, Explore, Search, Detail, Episodes
- Watchlist and History
- Deep-link episode player shell
- Jikan metadata integration
- Node.js backend API
- Episode detection worker
- UI and source-integration contracts

Metadata and video sources are intentionally separated. No DRM bypass, protected-stream extraction, or unauthorized re-hosting is implemented.

## Project
- app/ — Android
- backend/ — API + worker
- docs/ — project contract

## Build
Open this repository as an Android project with JDK 17 and Android SDK 36.

Backend:
```bash
cd backend
npm start
```

Worker:
```bash
npm run worker
```

Status: all-in foundation milestone.
