# Substracker

Substracker adalah aplikasi Android yang dirancang untuk membantu pengguna mengelola dan melacak langganan digital mereka dengan mudah. Aplikasi ini dibangun menggunakan teknologi Android untuk memastikan performa yang efisien dan pengalaman pengguna yang intuitif.

## Fitur Utama
- **Manajemen Langganan**: Menambah, melihat, dan melacak daftar langganan aktif.
- **Kategorisasi**: Pengelompokan langganan berdasarkan kategori (Entertainment, Productivity, Education, dll.).
- **Pengaturan Siklus**: Mendukung siklus penagihan Mingguan, Bulanan, dan Tahunan.
- **Pengingat**: Fitur untuk mengaktifkan pengingat pembayaran agar tidak melewatkan tanggal jatuh tempo.
- **Pencatatan**: Fitur untuk menambahkan catatan tambahan pada setiap langganan.

## Teknologi yang Digunakan
- **Bahasa**: Kotlin
- **Framework UI**: XML Layouts
- **Database**: Room Persistence Library
- **Arsitektur**: MVVM
- **Dependency Management**: Gradle (Kotlin DSL)

## Struktur Proyek
- `data/`: Berisi entitas database (`Subscription.kt`), DAO (`SubscriptionDao.kt`), dan konfigurasi database.
- `res/`: Berisi aset UI, desain warna, tema, dan drawable kustom.

## Panduan Memulai
Untuk menjalankan proyek ini di Android Studio:
1. Pastikan Anda telah menginstal Android Studio versi terbaru.
2. Clone repository ini ke komputer lokal Anda.
3. Buka Android Studio, pilih **Open**, lalu arahkan ke folder root proyek (folder `Substracker`). Android Studio akan secara otomatis mengenali file konfigurasi Gradle.
4. Tunggu hingga proses *Gradle Sync* selesai.
5. Jalankan aplikasi pada emulator atau perangkat Android fisik melalui menu **Run**.
