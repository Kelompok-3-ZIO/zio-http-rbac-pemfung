package siak.contoh

import zio.*

// Contoh ZLayer: koneksi database butuh Config, dan program butuh koneksi itu.
// Database di sini simulasi, supaya contoh jalan tanpa PostgreSQL.
// Jalankan: scala-cli run src/project.scala contoh/ContohKoneksi.scala
case class Config(host: String, port: Int)
trait Database:
  def query(sql: String): String

object ContohKoneksi extends ZIOAppDefault:

  // 1. ULayer: tidak butuh apa-apa, tidak bisa gagal, menghasilkan Config.
  val configLayer: ULayer[Config] =
    ZLayer.succeed(Config("localhost", 5432))

  // 2. ZLayer: butuh Config, bisa gagal saat koneksi dibuka, menghasilkan Database.
  val dbLayer: ZLayer[Config, Exception, Database] =
    ZLayer.fromZIO:
      for
        config <- ZIO.service[Config]
        _ <- Console.printLine(s"membuka koneksi ke ${config.host}:${config.port}")
      yield new Database:
        def query(sql: String) = s"hasil '$sql' dari ${config.host}"

  // 3. ZIO: program butuh Database, bisa gagal dengan Exception, menghasilkan String.
  val program: ZIO[Database, Exception, String] =
    ZIO.serviceWith[Database](_.query("SELECT 1"))

  // configLayer disuplai ke dbLayer, lalu hasilnya disuplai ke program.
  val run = program.provide(configLayer >>> dbLayer).flatMap(Console.printLine(_))
