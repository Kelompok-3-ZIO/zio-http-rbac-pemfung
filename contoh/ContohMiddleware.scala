package siak.contoh

import zio.*
import zio.http.*

// Dua middleware buatan sendiri, sebelum masuk ke RBAC.
// Jalankan: scala-cli run src/project.scala contoh/ContohMiddleware.scala
// Coba:     curl -i localhost:8082/halo/Pemfung
object ContohMiddleware extends ZIOAppDefault:

  val routes: Routes[Any, Response] = Routes(
    Method.GET / "halo" / string("nama") -> handler { (nama: String, _: Request) =>
      Response.text(s"Halo, $nama!")
    }
  )

  // Bekerja di sisi keluar: setiap Response diberi header tambahan.
  val tambahHeader: HandlerAspect[Any, Unit] =
    HandlerAspect.interceptOutgoingHandler(
      Handler.fromFunction[Response](_.addHeader("X-Kelompok", "3"))
    )

  // Bekerja di sisi masuk: mencatat request lalu meneruskannya.
  val catatRequest: HandlerAspect[Any, Unit] =
    HandlerAspect.interceptIncomingHandler(
      Handler.fromFunctionZIO[Request] { req =>
        Console.printLine(s"masuk: ${req.method} ${req.path}").orDie.as((req, ()))
      }
    )

  val run =
    Server.serve(routes @@ tambahHeader @@ catatRequest).provide(Server.defaultWithPort(8082))
