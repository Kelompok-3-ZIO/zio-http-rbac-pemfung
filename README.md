# ZIO HTTP dan RBAC: SIAK mini

Kode demo subtopik 2 Pemrograman Fungsional (kelompok 3, Topik A: ZIO HTTP). Isinya server HTTP kecil untuk data nilai mahasiswa. Server ini menunjukkan bagaimana paradigma FP dipakai saat mengatur hak akses berbasis role (RBAC, *role-based access control*): handler sebagai fungsi, middleware sebagai higher-order function, dan aturan akses sebagai pure function.

Model `User` dan `Role` sama dengan demo subtopik 1, ditambah `Aksi` dan `Nilai`.

## Isi repo

| Berkas | Isi |
|---|---|
| `src/Domain.scala` | `Role`, `User`, `Aksi`, `Nilai`, dan `Rbac`: seluruh aturan akses dalam bentuk pure function |
| `src/Repo.scala` | `UserRepo` dan `NilaiRepo` beserta `ZLayer`-nya |
| `src/Akses.scala` | Middleware `autentikasi` (401) dan `butuh(aksi)` (403) |
| `src/Api.scala` | Routes untuk tiga endpoint |
| `src/Main.scala` | Menjalankan server di port 8080 |
| `test/` | Test aturan akses dan matriks akses, semuanya tanpa membuka port |
| `contoh/Naif.scala` | Versi rawan yang mengecek role di tiap handler, sebagai pembanding |
| `contoh/LupaAutentikasi.scala` | Sengaja gagal compile, untuk menunjukkan compiler menolak routes tanpa autentikasi |
| `demo.sh` | Empat skenario curl untuk demo presentasi |
| `GLOSSARY.md` | Arti istilah yang dipakai |

## Menjalankan

Yang perlu dipasang cuma [scala-cli](https://scala-cli.virtuslab.org/install). Java tidak perlu dipasang terpisah, karena scala-cli akan mengunduh JVM 17 sendiri saat pertama kali dijalankan.

```bash
scala-cli run src
```

Jalankan perintah di bawah ini sekali sebelum presentasi, supaya JVM dan dependency sudah tersimpan di cache dan demo tidak bergantung pada internet kelas:

```bash
scala-cli compile src test
```

## Data contoh

| Id | Nama | Role | Token |
|---|---|---|---|
| 1 | Ani | Admin | `token-ani` |
| 2 | Budi | Dosen | `token-budi` |
| 3 | Cici | Mahasiswa | `token-cici` |
| 4 | Dedi | Mahasiswa | `token-dedi` |

Data disimpan di memori, jadi setiap kali server dijalankan ulang, nilainya kembali ke awal.

## Endpoint dan matriks akses

| Endpoint | Aksi | Admin | Dosen | Mahasiswa |
|---|---|---|---|---|
| `GET /mahasiswa/{id}/nilai` | LihatNilai | 200 | 200 | 200 untuk id sendiri, 403 untuk id lain |
| `PUT /mahasiswa/{id}/nilai` | UbahNilai | 200 | 200 | 403 |
| `GET /admin/users` | KelolaUser | 200 | 403 | 403 |

Request tanpa token atau dengan token yang tidak dikenal selalu mendapat 401. Id mahasiswa yang tidak ada mendapat 404.

Body untuk `PUT` berupa JSON:

```json
{"mataKuliah": "Pemfung", "huruf": "A"}
```

## Demo

Jalankan server di satu terminal, lalu `demo.sh` di terminal lain. Script ini berhenti di tiap skenario sampai enter ditekan.

```bash
./demo.sh
```

Contoh hasil skenario 2, mahasiswa mencoba mengubah nilai:

```text
$ curl -i -X PUT -H "Authorization: Bearer token-cici" -d '{"mataKuliah":"Pemfung","huruf":"A"}' localhost:8080/mahasiswa/3/nilai
HTTP/1.1 403 Forbidden
content-length: 36

Mahasiswa tidak punya izin UbahNilai
```

## Test

```bash
scala-cli test src test
```

Ada 18 test: 4 untuk aturan di `Rbac` dan 14 untuk tiap sel matriks akses, termasuk path yang tidak cocok dengan route mana pun. Test matriks memanggil `Api.routes` langsung dengan `Request` buatan. Cara ini bisa dipakai karena `Routes` pada dasarnya fungsi dari `Request` ke `Response`.

## Lupa memasang autentikasi

`Api.tanpaAutentikasi` adalah routes yang belum dibungkus middleware autentikasi, sehingga tipenya masih `Routes[User & NilaiRepo & UserRepo, Response]`. Kalau routes itu langsung dijalankan server, compiler menolak:

```bash
scala-cli compile src contoh/LupaAutentikasi.scala
```

```text
──── ZLAYER ERROR ────────────────────────────────────────────────────

 Please provide a layer for the following type:

   1. siak.User
```

Pembandingnya ada di `contoh/Naif.scala`: endpoint `DELETE` di sana lupa mengecek role, tetapi tetap lolos compile.

## Konsep FP di kode ini

| Konsep | Wujud di kode |
|---|---|
| Pure function | `Rbac.izin`, `Rbac.boleh`, `Rbac.bolehLihatNilaiMilik` |
| ADT dan pattern matching | `Role`, `Aksi`, dan pola route `Method.GET / "mahasiswa" / int("id") / "nilai"` |
| Higher-order function | `Akses.autentikasi` dan `Akses.butuh(aksi)`, dipasang dengan `@@` |
| Effect sebagai value | Setiap handler mengembalikan `ZIO[R, Response, Response]` |
| Typed environment dan ZLayer | `User` muncul di R sampai autentikasi menyediakannya; `UserRepo.live`, `NilaiRepo.live`, `Server.defaultWithPort` |
| Immutable state | `NilaiRepo` memakai `Ref`, sehingga perubahan nilai terjadi secara atomik tanpa variabel yang di-mutate |

## Catatan untuk production

Token di demo berupa *opaque token* yang dicari di server. Pola ini sah dipakai, asalkan tokennya acak, panjang, dan punya masa berlaku. Alternatif yang umum adalah JWT yang ditandatangani server dan punya masa berlaku. Untuk beralih ke JWT, yang perlu diganti hanya `Akses.autentikasi`, sedangkan handler dan aturan `Rbac` tetap sama.

## Versi

Scala 3.3.7, ZIO 2.1.27, ZIO HTTP 3.11.6, JVM 17.

## Kelompok 3

Danniel, Vincent Davis Leonard Tjoeng, Arya Raditya Kusuma, Clarence Grady, Fazle Ilahi Bimo Aji.
