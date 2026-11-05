# Glosarium

Istilah yang dipakai di kode, slide, dan diskusi kelompok 3. Kalau sebuah istilah dipakai dengan arti lain, perbaiki di sini dulu.

**User** (pengguna)
Orang yang memakai SIAK. Setiap user punya tepat satu role.

**Role**
Peran user di SIAK: Admin, Dosen, atau Mahasiswa. Role tidak dicek langsung oleh endpoint; yang dicek adalah aksi.

**Aksi** (*permission*)
Tindakan yang bisa diminta ke sistem: LihatNilai, UbahNilai, KelolaUser. Setiap endpoint menyatakan aksi yang dibutuhkannya.

**Izin**
Pemetaan dari role ke kumpulan aksi yang diperbolehkan. Aksi yang tidak tercantum untuk sebuah role berarti ditolak.

**Autentikasi**
Menjawab "kamu siapa?" dari token yang dibawa request. Gagal autentikasi menghasilkan 401 Unauthorized.

**Otorisasi**
Menjawab "kamu boleh apa?" berdasarkan izin role milik user yang sudah terautentikasi. Gagal otorisasi menghasilkan 403 Forbidden.

**Token**
Tanda pengenal yang dikirim di header `Authorization: Bearer <token>` dan dipetakan ke satu user.

**Nilai**
Huruf mutu satu mata kuliah milik seorang mahasiswa.

**Kepemilikan** (*object-level authorization*)
Aturan yang bergantung pada data yang diminta, bukan hanya role. Contohnya, mahasiswa punya aksi LihatNilai tetapi hanya untuk nilainya sendiri.
