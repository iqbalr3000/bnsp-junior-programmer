# Sistem Informasi Manajemen Rumah Sakit

Aplikasi desktop untuk manajemen data rumah sakit — dibuat sebagai demonstrasi kompetensi BNSP Junior Programmer (analisis tools, rancangan entitas & relasi, SQL, akses basis data, repository pattern, konfigurasi environment, keamanan informasi, dsb).

## Fitur

- Login staff (username/password, ter-hash BCrypt)
- Dashboard ringkasan statistik (total pasien, dokter, obat, reservasi, rekam medis)
- CRUD Data Pasien
- CRUD Data Dokter
- CRUD Data Obat
- Reservasi (booking pasien ke dokter, cegah bentrok jadwal dokter)
- Rekam Medis + resep obat (stok obat otomatis berkurang saat resep dibuat, dalam satu transaksi JDBC)
- Status bar pemantauan resource aplikasi (memory & koneksi database)
- AI Assistant (Gemini) — tanya jawab berbasis data aplikasi lewat tool calling read-only (cari pasien/dokter, cek stok obat, reservasi hari ini, riwayat rekam medis), dengan sliding window context dan riwayat chat tersimpan per staff

## Tech Stack

| Layer | Teknologi |
|---|---|
| UI | JavaFX 21 + [AtlantaFX](https://github.com/mkpaz/atlantafx) (tema PrimerLight) |
| Akses data | JDBC murni (`PreparedStatement`) + repository pattern — **bukan** ORM, supaya SQL terlihat eksplisit |
| Connection pool | HikariCP |
| Database | PostgreSQL 16 (Docker) |
| Migration | Flyway |
| Keamanan password | BCrypt (jBCrypt) |
| AI Assistant | Gemini API via official SDK [`google-genai`](https://github.com/googleapis/java-genai) |
| Testing | JUnit 5 + Mockito |
| Build | Maven |

## Arsitektur

Layered architecture, murni Java (tanpa framework DI):

```
model        -> POJO entitas (Pasien, Dokter, Obat, Reservasi, RekamMedis, Staff, dst.)
repository    -> interface + implementasi JDBC per entity (repository pattern, tanpa generic base class)
service       -> validasi & business logic (termasuk transaksi manual resep obat + potong stok)
config        -> AppConfig (env layering), DataSourceProvider (HikariCP), FlywayRunner
ai            -> wrapper Gemini SDK (GeminiClient/Impl) + deklarasi & dispatch tool read-only (AssistantToolExecutor)
ui            -> FXML + controller (JavaFX), dependency injection manual lewat AppContext + ContextAware
```

Composition root ada di `App.java` — semua repository & service di-instantiate manual di sana lalu disebar ke controller lewat `AppContext`.

## Prasyarat

- JDK 17+
- Maven 3.9+
- Docker & Docker Compose

## Menjalankan Aplikasi

1. Nyalakan database PostgreSQL (container dedicated untuk project ini, port `5432`, database `hospital_db`):

   ```bash
   docker compose up -d
   ```

2. Jalankan aplikasi (migration Flyway otomatis jalan duluan sebelum UI tampil):

   ```bash
   mvn javafx:run
   ```

3. Login dengan akun demo (di-seed lewat migration `V2__seed_staff.sql`):

   ```
   Username: admin
   Password: admin123
   ```

Fitur **AI Assistant** butuh `GEMINI_API_KEY` (lihat bagian Konfigurasi Environment) — kalau belum diset, aplikasi tetap jalan normal untuk fitur lain, menu AI Assistant hanya akan menampilkan status nonaktif.

## Konfigurasi Environment

Environment aktif ditentukan lewat variabel `APP_ENV` (`development` default kalau tidak di-set):

```bash
APP_ENV=staging mvn javafx:run
```

Urutan resolusi konfigurasi (`AppConfig`):
1. `application.properties` (default umum)
2. `application-{env}.properties` (override sesuai environment — `development`/`staging`/`production`)
3. Environment variable `DB_URL` / `DB_USERNAME` / `DB_PASSWORD` / `GEMINI_API_KEY` (override terakhir, dipakai untuk secret — tidak pernah ditulis di file manapun)

Contoh menjalankan dengan AI Assistant aktif:

```bash
export GEMINI_API_KEY=xxxxx
mvn javafx:run
```

Property `gemini.model` (default `gemini-2.5-flash`) dan `gemini.contextWindowSize` (default `20` pesan — jumlah pesan terakhir yang di-assemble jadi context request ke Gemini, bukan pembatas riwayat yang ditampilkan di UI) bisa diubah di `application.properties`.

## Testing

```bash
mvn test
```

Unit test (JUnit 5 + Mockito) fokus di service layer — semua repository/DataSource di-mock, tidak butuh koneksi database nyata. Yang paling penting: `RekamMedisServiceTest` memverifikasi transaksi commit/rollback saat stok obat cukup/tidak cukup.

## Struktur Database

Skema didefinisikan di `src/main/resources/db/migration/V1__init_schema.sql`:

- `staff` — akun login
- `pasien`, `dokter`, `obat` — master data
- `reservasi` — booking pasien ke dokter (unique index mencegah dokter double-booked di slot yang sama)
- `rekam_medis` — hasil pemeriksaan (opsional terhubung ke reservasi)
- `rekam_medis_obat` — resep obat per rekam medis (many-to-many rekam_medis ↔ obat)
- `chat_message` (`V3`) — riwayat chat AI Assistant per staff (role USER/MODEL, `tool_trace` untuk audit tool call)

## Menghentikan / Membersihkan

```bash
docker compose down        # stop container, data tetap ada di volume
docker compose down -v     # stop + hapus data
```
