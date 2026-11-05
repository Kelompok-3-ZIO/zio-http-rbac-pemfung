package siak.contoh

import zio.*
import zio.http.*

// Server ZIO HTTP paling kecil.
// Jalankan: scala-cli run src/project.scala contoh/ContohHalo.scala
// Coba:     curl localhost:8081/halo/Pemfung
object ContohHalo extends ZIOAppDefault:

  val routes: Routes[Any, Response] = Routes(
    Method.GET / "halo" / string("nama") -> handler { (nama: String, _: Request) =>
      Response.text(s"Halo, $nama!")
    }
  )

  val run = Server.serve(routes).provide(Server.defaultWithPort(8081))
