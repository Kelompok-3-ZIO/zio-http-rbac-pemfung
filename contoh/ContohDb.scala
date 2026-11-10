package siak.contoh

import zio.*
import zio.http.*

// Contoh: koneksi database dibuka sekali saat server mulai dan ditutup saat server berhenti.
// Database di sini simulasi (Map di memori), supaya contoh jalan tanpa PostgreSQL.
// Jalankan: scala-cli run src/project.scala contoh/ContohDb.scala
// Coba:     curl localhost:8083/nilai/3
trait Database:
  def cariNilai(id: Int): UIO[Option[String]]

class DbSimulasi extends Database:
  private val data = Map(3 -> "A", 4 -> "B")
  def cariNilai(id: Int): UIO[Option[String]] = ZIO.succeed(data.get(id))

object Database:
  // acquireRelease: langkah pertama membuka koneksi, langkah kedua menutupnya.
  private val buka  = Console.printLine("membuka koneksi DB").orDie.as(DbSimulasi())
  private val tutup = (_: DbSimulasi) => Console.printLine("menutup koneksi DB").orDie

  val live: ULayer[Database] = ZLayer.scoped(ZIO.acquireRelease(buka)(tutup))

object ContohDb extends ZIOAppDefault:

  // Handler hanya menyatakan kebutuhannya (R = Database), tanpa tahu cara koneksi dibuat.
  val routes: Routes[Database, Response] = Routes(
    Method.GET / "nilai" / int("id") -> handler { (id: Int, _: Request) =>
      ZIO.serviceWithZIO[Database](_.cariNilai(id)).map {
        case Some(huruf) => Response.text(s"Nilai mahasiswa $id: $huruf")
        case None        => Response.notFound
      }
    }
  )

  val run = Server.serve(routes).provide(Server.defaultWithPort(8083), Database.live)
