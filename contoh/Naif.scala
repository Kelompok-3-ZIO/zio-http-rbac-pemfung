package siak.contoh

import siak.*
import zio.*
import zio.http.*

// Versi yang rawan, untuk dibandingkan di slide. Tidak dipakai oleh Main.
// Kompilasi: scala-cli compile src contoh/Naif.scala
object Naif:

  // Disalin ke setiap handler yang butuh user.
  def userDariHeader(req: Request): ZIO[UserRepo, Response, User] =
    req.header(Header.Authorization) match
      case Some(Header.Authorization.Bearer(token)) =>
        ZIO.serviceWithZIO[UserRepo](_.cariDariToken(token.stringValue)).someOrFail(Response.unauthorized)
      case _ => ZIO.fail(Response.unauthorized)

  val routes: Routes[UserRepo & NilaiRepo, Response] = Routes(
    Method.PUT / "mahasiswa" / int("id") / "nilai" -> handler { (id: Int, req: Request) =>
      for
        user <- userDariHeader(req)
        _    <- ZIO.when(user.role == Role.Mahasiswa)(ZIO.fail(Response.forbidden))
        _    <- ZIO.serviceWithZIO[NilaiRepo](_.ubah(id, Nilai("Pemfung", "A")))
      yield Response.ok
    },
    // Endpoint baru, pengecekan role lupa disalin. Tetap lolos compile.
    Method.DELETE / "mahasiswa" / int("id") / "nilai" -> handler { (id: Int, req: Request) =>
      for
        user <- userDariHeader(req)
        _    <- ZIO.serviceWithZIO[NilaiRepo](_.ubah(id, Nilai("Pemfung", "E")))
      yield Response.ok
    }
  )
