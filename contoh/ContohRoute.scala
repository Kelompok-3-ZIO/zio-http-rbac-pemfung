package siak.contoh

import zio.*
import zio.http.*

// Dua route untuk melihat pola route dan segmen bertipe Int. Tanpa autentikasi.
// Jalankan: scala-cli run src/project.scala contoh/ContohRoute.scala
// Coba:     curl -i localhost:8085/mahasiswa/3/nilai
//           curl -i localhost:8085/mahasiswa/abc/nilai
//           curl -i localhost:8085/admin/users
object ContohRoute extends ZIOAppDefault:

  val routes: Routes[Any, Response] = Routes(
    Method.GET / "mahasiswa" / int("id") / "nilai" -> handler { (id: Int, _: Request) =>
      Response.text(s"Nilai mahasiswa $id")
    },
    Method.GET / "admin" / "users" -> handler { (_: Request) =>
      Response.text("Daftar user")
    }
  )

  val run = Server.serve(routes).provide(Server.defaultWithPort(8085))
