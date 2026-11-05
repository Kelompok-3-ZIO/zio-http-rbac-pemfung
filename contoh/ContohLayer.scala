package siak.contoh

import zio.*

// Contoh kecil ZLayer: dua service butuh Config yang sama.
// Jalankan: scala-cli run src/project.scala contoh/ContohLayer.scala
case class Config(nama: String)

case class Sapaan(config: Config):
  def halo: UIO[String] = ZIO.succeed(s"Halo, ${config.nama}!")

case class Pamit(config: Config):
  def bye: UIO[String] = ZIO.succeed(s"Sampai jumpa, ${config.nama}!")

object ContohLayer extends ZIOAppDefault:

  // Layer yang pembuatannya punya efek: mencetak saat dibangun.
  val configLayer: ULayer[Config] =
    ZLayer(Console.printLine("membangun Config").orDie.as(Config("Pemfung")))

  // Kebutuhan dibaca dari parameter konstruktor.
  val sapaanLayer: URLayer[Config, Sapaan] = ZLayer.fromFunction(Sapaan.apply)
  val pamitLayer: URLayer[Config, Pamit]   = ZLayer.fromFunction(Pamit.apply)

  val program: ZIO[Sapaan & Pamit, java.io.IOException, Unit] =
    for
      a <- ZIO.serviceWithZIO[Sapaan](_.halo)
      b <- ZIO.serviceWithZIO[Pamit](_.bye)
      _ <- Console.printLine(a) *> Console.printLine(b)
    yield ()

  // Config dibutuhkan dua layer, tetapi hanya dibangun sekali.
  val run = program.provide(configLayer, sapaanLayer, pamitLayer)
