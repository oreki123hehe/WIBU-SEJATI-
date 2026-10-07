# Wibu Sejati — Architecture

## Android
Navigasi utama:

Home → Anime → Detail → Episode → Player

Fitur:
- Home
- Search
- Filter/kategori
- Detail anime
- Daftar episode
- Player
- Watchlist
- Riwayat tontonan
- Notifikasi
- Deep link episode

Loading, empty, error, dan unavailable-source states harus disiapkan sejak awal.

## Backend
Backend menangani:
1. katalog metadata
2. pencarian
3. anime dan episode
4. sinkronisasi episode
5. status sumber tontonan
6. push notification
7. API client

## Pemisahan model

Anime:
- id
- title
- poster
- synopsis
- year
- genres

Episode:
- id
- animeId
- number
- title
- publishedAt
- viewingSource

Metadata tidak boleh digabung dengan logika player.

## Update otomatis
Source/API → detector → validation → database → notification → deep link → episode.

## Sumber tontonan
Player hanya boleh memakai sumber yang penggunaannya memang diizinkan oleh penyedia sumber. Tidak ada desain untuk bypass DRM, scraping stream terlindungi, atau redistribusi file.

## UI lock
Perubahan backend tidak boleh memaksa perubahan besar pada UI/navigasi. Kontrak data menjadi lapisan penghubung.
