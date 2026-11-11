# ZIO HTTP dan RBAC: SIAK mini

Kode demo subtopik 2 Pemrograman Fungsional (kelompok 3, Topik A: ZIO HTTP). Isinya server HTTP kecil untuk data nilai mahasiswa. Server ini menunjukkan bagaimana paradigma FP dipakai saat mengatur hak akses berbasis role (RBAC, *role-based access control*): handler sebagai fungsi, middleware sebagai higher-order function, dan aturan akses sebagai pure function.

Model `User` dan `Role` sama dengan demo subtopik 1, ditambah `Aksi` dan `Nilai`. Selain demo utama, ada beberapa contoh kecil yang bisa dijalankan sendiri.

**Tidak memasang compiler?** Semua perintah di README ini disertai hasil yang diharapkan, diambil dari run asli. Bagian [Hasil yang diharapkan](#hasil-yang-diharapkan) bisa dibaca tanpa menjalankan apa pun.

## Isi repo

| Berkas | Isi |
|---|---|
| `src/Domain.scala` | `Role`, `User`, `Aksi`, `Nilai`, dan `Rbac`: seluruh aturan akses dalam bentuk pure function |
| `src/Repo.scala` | `UserRepo` dan `NilaiRepo` beserta `ZLayer`-nya |
| `src/Akses.scala` | Middleware `autentikasi` (401) dan `butuh(aksi)` (403) |
| `src/Api.scala` | Routes untuk tiga endpoint |
| `src/Main.scala` | Menjalankan server di port 8080 |
| `test/` | Test aturan akses dan matriks akses, semuanya tanpa membuka port |
| `contoh/ContohLayer.scala` | Contoh kecil ZLayer: dua service memakai satu Config yang dibangun sekali |
| `contoh/ContohKoneksi.scala` | Layer koneksi database yang butuh `Config`, digabung dengan `>>>` (database simulasi) |
| `contoh/ContohDb.scala` | Layer database dipakai server ZIO HTTP: koneksi dibuka sekali saat server mulai dan ditutup saat server berhenti |
| `contoh/ContohHalo.scala` | Server ZIO HTTP paling kecil, satu route |
| `contoh/ContohRoute.scala` | Dua route: pola `int("id")` dan route statis, tanpa autentikasi |
| `contoh/ContohMiddleware.scala` | Dua middleware buatan sendiri: menambah header dan mencatat request |
| `contoh/Naif.scala` | Versi rawan yang mengecek role di tiap handler, sebagai pembanding |
| `contoh/TipeHandler.scala` | Handler `lihatNilai` dengan tipe `Handler[R, Err, In, Out]` ditulis eksplisit (di `src/Api.scala` tipenya disimpulkan compiler) |
| `contoh/LupaAutentikasi.scala` | Sengaja gagal compile, untuk menunjukkan compiler menolak routes tanpa autentikasi |
| `demo.sh` | Empat skenario curl untuk demo presentasi |
| `GLOSSARY.md` | Arti istilah yang dipakai |

## Cara menjalankan

### 1. Pasang scala-cli

Yang perlu dipasang hanya [scala-cli](https://scala-cli.virtuslab.org/install). Java tidak perlu dipasang terpisah, karena scala-cli mengunduh JVM 17 sendiri saat pertama kali dijalankan.

macOS dengan Homebrew:

```bash
brew install Virtuslab/scala-cli/scala-cli
```

macOS atau Linux tanpa Homebrew:

```bash
curl -sSLf https://scala-cli.virtuslab.org/get | sh
```

Untuk Windows, ikuti petunjuk di [halaman instalasi scala-cli](https://scala-cli.virtuslab.org/install).

### 2. Unduh dependency sekali

Run pertama mengunduh JVM, Scala, ZIO, dan ZIO HTTP (sekitar beberapa menit, tergantung internet). Setelah itu semuanya tersimpan di cache.

```bash
scala-cli compile src test
```

### 3. Jalankan

| Tujuan | Perintah | Port |
|---|---|---|
| Demo SIAK | `scala-cli run src` | 8080 |
| Test | `scala-cli test src test` | tidak ada |
| Contoh ZLayer | `scala-cli run src/project.scala contoh/ContohLayer.scala` | tidak ada |
| Contoh koneksi database | `scala-cli run src/project.scala contoh/ContohKoneksi.scala` | tidak ada |
| Contoh database di server | `scala-cli run src/project.scala contoh/ContohDb.scala` | 8083 |
| Contoh server kecil | `scala-cli run src/project.scala contoh/ContohHalo.scala` | 8081 |
| Contoh route | `scala-cli run src/project.scala contoh/ContohRoute.scala` | 8085 |
| Contoh middleware | `scala-cli run src/project.scala contoh/ContohMiddleware.scala` | 8082 |
| Contoh gagal compile | `scala-cli compile src contoh/LupaAutentikasi.scala` | tidak ada |

Server berhenti dengan Ctrl+C. Berkas di `contoh/` dijalankan satu per satu. Beberapa memakai nama yang sama (`Config`, `Database`), jadi jangan dikompilasi bersamaan.

## Hasil yang diharapkan

Header `date` pada output curl di bawah tentu berbeda setiap kali dijalankan. Selain itu, outputnya harus sama persis.

### Contoh ZLayer

```bash
scala-cli run src/project.scala contoh/ContohLayer.scala
```

```text
membangun Config
Halo, Pemfung!
Sampai jumpa, Pemfung!
```

`membangun Config` hanya tercetak sekali walaupun `Sapaan` dan `Pamit` sama-sama butuh `Config`, karena layer di dalam satu `provide` dibangun sekali lalu dipakai bersama.

### Contoh koneksi database

```bash
scala-cli run src/project.scala contoh/ContohKoneksi.scala
```

```text
membuka koneksi ke localhost:5432
hasil 'SELECT 1' dari localhost
```

`configLayer >>> dbLayer` menyuplai `Config` ke layer database, lalu hasilnya dipakai `program`. Database di contoh ini simulasi, jadi tidak perlu PostgreSQL.

### Contoh database di server

Jalankan `scala-cli run src/project.scala contoh/ContohDb.scala`. Sebelum server mulai, terminal mencetak:

```text
membuka koneksi DB
```

Di terminal lain:

```bash
curl localhost:8083/nilai/3
curl -i localhost:8083/nilai/9
```

```text
Nilai mahasiswa 3: A
HTTP/1.1 404 Not Found
```

Koneksi hanya dibuka sekali walaupun ada banyak request. Saat server dihentikan dengan Ctrl+C, terminal server mencetak `menutup koneksi DB`.

### Contoh server kecil

Jalankan `scala-cli run src/project.scala contoh/ContohHalo.scala`, lalu di terminal lain:

```bash
curl localhost:8081/halo/Pemfung
```

```text
Halo, Pemfung!
```

### Contoh middleware

Jalankan `scala-cli run src/project.scala contoh/ContohMiddleware.scala`, lalu di terminal lain:

```bash
curl -i localhost:8082/halo/Pemfung
```

```text
HTTP/1.1 200 OK
content-type: text/plain
x-kelompok: 3
date: Thu, 05 Nov 2026 02:33:09 GMT
content-length: 14

Halo, Pemfung!
```

Header `x-kelompok: 3` ditambahkan middleware `tambahHeader`. Di terminal server, middleware `catatRequest` mencetak:

```text
masuk: GET /halo/Pemfung
```

### Demo SIAK

Jalankan `scala-cli run src`. Server mencetak `SIAK mini jalan di http://localhost:8080`. Di terminal lain, jalankan `./demo.sh` (berhenti di tiap skenario sampai enter ditekan), atau perintah curl di bawah satu per satu.

Data contoh:

| Id | Nama | Role | Token |
|---|---|---|---|
| 1 | Ani | Admin | `token-ani` |
| 2 | Budi | Dosen | `token-budi` |
| 3 | Cici | Mahasiswa | `token-cici` |
| 4 | Dedi | Mahasiswa | `token-dedi` |

Data disimpan di memori, jadi setiap kali server dijalankan ulang, nilainya kembali ke awal (Cici B, Dedi A-).

**1. Tanpa token: 401**

```bash
curl -i localhost:8080/mahasiswa/3/nilai
```

```text
HTTP/1.1 401 Unauthorized
www-authenticate: Bearer realm="SIAK"
content-length: 0
```

**2. Mahasiswa mengubah nilai: 403**

```bash
curl -i -X PUT -H "Authorization: Bearer token-cici" -d '{"mataKuliah":"Pemfung","huruf":"A"}' localhost:8080/mahasiswa/3/nilai
```

```text
HTTP/1.1 403 Forbidden
content-length: 36

Mahasiswa tidak punya izin UbahNilai
```

**3. Dosen mengubah nilai: 200**

```bash
curl -i -X PUT -H "Authorization: Bearer token-budi" -d '{"mataKuliah":"Pemfung","huruf":"A"}' localhost:8080/mahasiswa/3/nilai
```

```text
HTTP/1.1 200 OK
content-type: text/plain
content-length: 35

Nilai Pemfung mahasiswa 3 menjadi A
```

**4. Mahasiswa melihat nilai orang lain: 403**

```bash
curl -i -H "Authorization: Bearer token-cici" localhost:8080/mahasiswa/4/nilai
```

```text
HTTP/1.1 403 Forbidden
content-length: 46

Mahasiswa hanya boleh melihat nilainya sendiri
```

**5. Mahasiswa melihat nilainya sendiri: 200** (setelah skenario 3, nilainya sudah A)

```bash
curl -i -H "Authorization: Bearer token-cici" localhost:8080/mahasiswa/3/nilai
```

```text
HTTP/1.1 200 OK
content-type: application/json
content-length: 38

[{"mataKuliah":"Pemfung","huruf":"A"}]
```

**6. Admin melihat daftar user: 200**

```bash
curl -i -H "Authorization: Bearer token-ani" localhost:8080/admin/users
```

```text
HTTP/1.1 200 OK
content-type: application/json
content-length: 160

[{"id":1,"nama":"Ani","role":"Admin"},{"id":2,"nama":"Budi","role":"Dosen"},{"id":3,"nama":"Cici","role":"Mahasiswa"},{"id":4,"nama":"Dedi","role":"Mahasiswa"}]
```

### Matriks akses

| Endpoint | Aksi | Admin | Dosen | Mahasiswa |
|---|---|---|---|---|
| `GET /mahasiswa/{id}/nilai` | LihatNilai | 200 | 200 | 200 untuk id sendiri, 403 untuk id lain |
| `PUT /mahasiswa/{id}/nilai` | UbahNilai | 200 | 200 | 403 |
| `GET /admin/users` | KelolaUser | 200 | 403 | 403 |

Request tanpa token atau dengan token yang tidak dikenal selalu mendapat 401. Id mahasiswa yang tidak ada mendapat 404, begitu juga path yang tidak cocok dengan route mana pun (misalnya `/mahasiswa/abc/nilai`).

### Test

```bash
scala-cli test src test
```

ZIO Test menjalankan test secara paralel, jadi urutan baris bisa berbeda. Isi dan baris terakhirnya harus sama:

```text
+ Rbac
  + Mahasiswa hanya boleh melihat nilai miliknya
  + Dosen boleh mengubah nilai, tapi tidak boleh mengelola user
  + Admin punya semua izin
  + Mahasiswa hanya boleh melihat nilai
+ Matriks akses
  + Admin melihat nilai Cici -> 200
  + Dosen melihat nilai Cici -> 200
  + token tidak dikenal -> 401
  + tanpa token -> 401
  + Cici melihat nilainya sendiri -> 200
  + Cici melihat nilai Dedi -> 403
  + Dosen melihat mahasiswa 99 -> 404
  + id bukan angka, route tidak cocok -> 404
  + Cici mengubah nilainya sendiri -> 403
  + Dosen melihat daftar user -> 403
  + Dosen mengubah nilai Cici -> 200
  + Admin mengubah nilai Cici -> 200
  + Cici melihat daftar user -> 403
  + Admin melihat daftar user -> 200
18 tests passed. 0 tests failed. 0 tests ignored.
```

Test matriks memanggil `Api.routes` langsung dengan `Request` buatan. Cara ini bisa dipakai karena `Routes` pada dasarnya fungsi dari `Request` ke `Response`. Untuk memastikan test tidak lulus secara kebetulan, kami pernah sengaja memberi Dosen izin `KelolaUser` di `Rbac.izin`, dan 2 test langsung gagal.

### Lupa memasang autentikasi

`Api.tanpaAutentikasi` adalah routes yang belum dibungkus middleware autentikasi, sehingga tipenya masih `Routes[User & NilaiRepo & UserRepo, Response]`. Kalau routes itu langsung dijalankan server, compiler menolak:

```bash
scala-cli compile src contoh/LupaAutentikasi.scala
```

```text
──── ZLAYER ERROR ────────────────────────────────────────────────────

 Please provide a layer for the following type:

   1. siak.User

1 error found
Compilation failed
```

Pembandingnya ada di `contoh/Naif.scala`: endpoint `DELETE` di sana lupa mengecek role, tetapi tetap lolos compile.

## Konsep FP di kode ini

| Konsep | Wujud di kode |
|---|---|
| Pure function | `Rbac.izin`, `Rbac.boleh`, `Rbac.bolehLihatNilaiMilik` |
| ADT dan pattern matching | `Role`, `Aksi`, dan pola route `Method.GET / "mahasiswa" / int("id") / "nilai"` |
| Higher-order function | `tambahHeader`, `catatRequest`, `Akses.autentikasi`, dan `Akses.butuh(aksi)`, dipasang dengan `@@` |
| Effect sebagai value | Setiap handler mengembalikan `ZIO[R, Response, Response]`, dan kegagalan berupa `Response` |
| Typed environment dan ZLayer | `User` muncul di R sampai autentikasi menyediakannya; `UserRepo.live`, `NilaiRepo.live`, `Server.defaultWithPort` |
| State tanpa mutasi variabel | `NilaiRepo` memakai `Ref`, sehingga perubahan nilai terjadi secara atomik |

## Catatan untuk production

Token di demo berupa *opaque token* yang dicari di server. Pola ini sah dipakai, asalkan tokennya acak, panjang, dan punya masa berlaku. Alternatif yang umum adalah JWT yang ditandatangani server dan punya masa berlaku. Untuk beralih ke JWT, yang perlu diganti hanya `Akses.autentikasi`, sedangkan handler dan aturan `Rbac` tetap sama.

## Versi

Scala 3.3.7, ZIO 2.1.27, ZIO HTTP 3.11.6, JVM 17.

## Kelompok 3

Danniel, Vincent Davis Leonard Tjoeng, Arya Raditya Kusuma, Clarence Grady, Fazle Ilahi Bimo Aji.
