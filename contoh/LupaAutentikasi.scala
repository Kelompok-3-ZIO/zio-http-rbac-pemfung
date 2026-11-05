package siak.contoh

import siak.*
import zio.*
import zio.http.*

// Sengaja tidak lolos compile: routes tanpa middleware autentikasi masih butuh User.
// Kompilasi: scala-cli compile src contoh/LupaAutentikasi.scala
object LupaAutentikasi extends ZIOAppDefault:
  val run =
    Server
      .serve(Api.tanpaAutentikasi)
      .provide(Server.defaultWithPort(8080), UserRepo.live, NilaiRepo.live)
