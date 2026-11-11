package siak.contoh

import siak.*
import zio.*
import zio.http.*
import zio.schema.Schema
import zio.schema.codec.JsonCodec.schemaBasedBinaryCodec

// Handler lihatNilai dengan tipenya ditulis eksplisit. Di src/Api.scala tipe ini tidak ditulis,
// compiler menyimpulkannya dari isi handler. Kalau salah satu slot diganti, kode ini tidak compile.
// Kompilasi: scala-cli compile src contoh/TipeHandler.scala
object TipeHandler:

  private def json[A: Schema](a: A): Response =
    Response(body = Body.from(a)).addHeader(Header.ContentType(MediaType.application.json))

  val lihatNilai: Handler[User & NilaiRepo, Response, (Int, Request), Response] =
    handler { (id: Int, _: Request) =>
      for
        user  <- ZIO.service[User]
        _     <- ZIO.unless(Rbac.bolehLihatNilaiMilik(user, id))(
                   ZIO.fail(Response.forbidden("Mahasiswa hanya boleh melihat nilainya sendiri"))
                 )
        nilai <- ZIO.serviceWithZIO[NilaiRepo](_.lihat(id))
                   .someOrFail(Response.notFound(s"Mahasiswa $id tidak ditemukan"))
      yield json(nilai)
    }
