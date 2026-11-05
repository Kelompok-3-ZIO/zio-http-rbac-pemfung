package siak

import zio.test.*

// Aturan akses berupa pure function, jadi dites tanpa server dan tanpa effect.
object RbacSpec extends ZIOSpecDefault:
  val cici = User(3, "Cici", Role.Mahasiswa)

  def spec = suite("Rbac")(
    test("Admin punya semua izin") {
      assertTrue(Rbac.izin(Role.Admin) == Aksi.values.toSet)
    },
    test("Dosen boleh mengubah nilai, tapi tidak boleh mengelola user") {
      assertTrue(
        Rbac.boleh(Role.Dosen, Aksi.UbahNilai),
        !Rbac.boleh(Role.Dosen, Aksi.KelolaUser)
      )
    },
    test("Mahasiswa hanya boleh melihat nilai") {
      assertTrue(Rbac.izin(Role.Mahasiswa) == Set(Aksi.LihatNilai))
    },
    test("Mahasiswa hanya boleh melihat nilai miliknya") {
      assertTrue(
        Rbac.bolehLihatNilaiMilik(cici, 3),
        !Rbac.bolehLihatNilaiMilik(cici, 4)
      )
    }
  )
