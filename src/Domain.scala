package siak

import zio.schema.{DeriveSchema, Schema}

// Model data yang sama dengan subtopik 1.
enum Role:
  case Admin, Dosen, Mahasiswa

case class User(id: Int, nama: String, role: Role)

// Tindakan yang bisa diminta ke sistem. Route menyebut Aksi, bukan nama Role.
enum Aksi:
  case LihatNilai, UbahNilai, KelolaUser

case class Nilai(mataKuliah: String, huruf: String)

object Nilai:
  given Schema[Nilai] = DeriveSchema.gen[Nilai]

object User:
  given Schema[Role] = DeriveSchema.gen[Role]
  given Schema[User] = DeriveSchema.gen[User]

// Seluruh aturan akses ada di sini, dalam bentuk pure function.
object Rbac:

  // Role ke permission. Aksi yang tidak tercantum berarti ditolak (deny by default).
  def izin(role: Role): Set[Aksi] = role match
    case Role.Admin     => Set(Aksi.LihatNilai, Aksi.UbahNilai, Aksi.KelolaUser)
    case Role.Dosen     => Set(Aksi.LihatNilai, Aksi.UbahNilai)
    case Role.Mahasiswa => Set(Aksi.LihatNilai)

  def boleh(role: Role, aksi: Aksi): Boolean =
    izin(role).contains(aksi)

  // Aturan level objek: mahasiswa hanya boleh melihat nilainya sendiri.
  def bolehLihatNilaiMilik(user: User, idMahasiswa: Int): Boolean =
    user.role != Role.Mahasiswa || user.id == idMahasiswa
