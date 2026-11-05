package siak

import zio.*
import zio.http.*
import zio.schema.Schema
import zio.schema.codec.JsonCodec.schemaBasedBinaryCodec

object Api:

  private def json[A: Schema](a: A): Response =
    Response(body = Body.from(a)).addHeader(Header.ContentType(MediaType.application.json))

  // Setiap kelompok route menyatakan izin yang dibutuhkan lewat @@ Akses.butuh(...).
  // Handler di dalamnya tidak lagi berisi pengecekan role.

  val lihatNilai: Routes[User & NilaiRepo, Response] = Routes(
    Method.GET / "mahasiswa" / int("id") / "nilai" -> handler { (id: Int, _: Request) =>
      for
        user  <- ZIO.service[User]
        _     <- ZIO.unless(Rbac.bolehLihatNilaiMilik(user, id))(
                   ZIO.fail(Response.forbidden("Mahasiswa hanya boleh melihat nilainya sendiri"))
                 )
        nilai <- ZIO.serviceWithZIO[NilaiRepo](_.lihat(id))
                   .someOrFail(Response.notFound(s"Mahasiswa $id tidak ditemukan"))
      yield json(nilai)
    }
  ) @@ Akses.butuh(Aksi.LihatNilai)

  val ubahNilai: Routes[User & NilaiRepo, Response] = Routes(
    Method.PUT / "mahasiswa" / int("id") / "nilai" -> handler { (id: Int, req: Request) =>
      for
        nilai <- req.body.to[Nilai].orElseFail(Response.badRequest("Body harus berupa JSON Nilai"))
        ada   <- ZIO.serviceWithZIO[NilaiRepo](_.ubah(id, nilai))
        _     <- ZIO.unless(ada)(ZIO.fail(Response.notFound(s"Mahasiswa $id tidak ditemukan")))
      yield Response.text(s"Nilai ${nilai.mataKuliah} mahasiswa $id menjadi ${nilai.huruf}")
    }
  ) @@ Akses.butuh(Aksi.UbahNilai)

  val kelolaUser: Routes[User & UserRepo, Response] = Routes(
    Method.GET / "admin" / "users" -> handler { (_: Request) =>
      ZIO.serviceWithZIO[UserRepo](_.semua).map(json)
    }
  ) @@ Akses.butuh(Aksi.KelolaUser)

  // Belum ada autentikasi: R masih menyebut User, jadi belum bisa dijalankan server.
  val tanpaAutentikasi: Routes[User & NilaiRepo & UserRepo, Response] =
    lihatNilai ++ ubahNilai ++ kelolaUser

  // Autentikasi menyediakan User, jadi User hilang dari R.
  val routes: Routes[NilaiRepo & UserRepo, Response] =
    tanpaAutentikasi.@@[NilaiRepo & UserRepo](Akses.autentikasi)
