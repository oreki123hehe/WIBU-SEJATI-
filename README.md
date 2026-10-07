# WIBU-SEJATI-

Aplikasi Android anime berbahasa Indonesia.

## Konsep
Wibu Sejati menggabungkan katalog anime, pencarian, detail, episode, pemutar, watchlist, riwayat, dan notifikasi episode baru dalam satu aplikasi.

## Prinsip arsitektur
- UI utama dirancang dan dikunci sejak awal.
- Backend dapat berkembang tanpa mengacak navigasi utama.
- Metadata anime dipisahkan dari sumber tontonan.
- Sumber tontonan hanya diintegrasikan jika penggunaan dan embedding-nya diizinkan.
- Aplikasi tidak meng-host ulang atau mengunduh konten berhak cipta tanpa izin.

## Struktur
- `app/` — Android application
- `backend/` — API dan sinkronisasi katalog/episode
- `docs/` — arsitektur, kontrak data, dan roadmap

## Status
Foundation selesai. Tahap berikutnya: Android project + backend contract.
