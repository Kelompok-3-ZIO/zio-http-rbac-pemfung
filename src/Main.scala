package siak

import zio.*
import zio.http.*

object Main extends ZIOAppDefault:
  val run =
    (Console.printLine("SIAK mini jalan di http://localhost:8080") *> Server.serve(Api.routes))
      .provide(Server.defaultWithPort(8080), UserRepo.live, NilaiRepo.live)
