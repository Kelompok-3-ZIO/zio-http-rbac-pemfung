package siak

import zio.*
import zio.http.*
import zio.schema.codec.JsonCodec.schemaBasedBinaryCodec
import zio.test.*

// Routes adalah fungsi Request => Response, jadi bisa dipanggil langsung tanpa membuka port.
object ApiSpec extends ZIOSpecDefault:

  def minta(method: Method, path: String, token: Option[String]): ZIO[NilaiRepo & UserRepo, Nothing, Status] =
    val headers = token.fold(Headers.empty)(t => Headers(Header.Authorization.Bearer(t)))
    val body    = if method == Method.PUT then Body.from(Nilai("Pemfung", "A")) else Body.empty
    ZIO.scoped(
      Api.routes
        .runZIO(Request(method = method, url = URL(Path.decode(path)), headers = headers, body = body))
        .map(_.status)
    )

  val ani  = Some("token-ani")
  val budi = Some("token-budi")
  val cici = Some("token-cici")

  // Satu baris = satu sel pada matriks akses di slide.
  val matriks: List[(String, Option[String], Method, String, Status)] = List(
    ("tanpa token",                     None,               Method.GET, "/mahasiswa/3/nilai", Status.Unauthorized),
    ("token tidak dikenal",             Some("token-palsu"), Method.GET, "/mahasiswa/3/nilai", Status.Unauthorized),
    ("Admin melihat nilai Cici",        ani,                Method.GET, "/mahasiswa/3/nilai", Status.Ok),
    ("Dosen melihat nilai Cici",        budi,               Method.GET, "/mahasiswa/3/nilai", Status.Ok),
    ("Cici melihat nilainya sendiri",   cici,               Method.GET, "/mahasiswa/3/nilai", Status.Ok),
    ("Cici melihat nilai Dedi",         cici,               Method.GET, "/mahasiswa/4/nilai", Status.Forbidden),
    ("Dosen melihat mahasiswa 99",      budi,               Method.GET, "/mahasiswa/99/nilai", Status.NotFound),
    ("Admin mengubah nilai Cici",       ani,                Method.PUT, "/mahasiswa/3/nilai", Status.Ok),
    ("Dosen mengubah nilai Cici",       budi,               Method.PUT, "/mahasiswa/3/nilai", Status.Ok),
    ("Cici mengubah nilainya sendiri",  cici,               Method.PUT, "/mahasiswa/3/nilai", Status.Forbidden),
    ("Admin melihat daftar user",       ani,                Method.GET, "/admin/users",       Status.Ok),
    ("Dosen melihat daftar user",       budi,               Method.GET, "/admin/users",       Status.Forbidden),
    ("Cici melihat daftar user",        cici,               Method.GET, "/admin/users",       Status.Forbidden)
  )

  def spec = suite("Matriks akses")(
    matriks.map { case (nama, token, method, path, harapan) =>
      test(s"$nama -> ${harapan.code}") {
        minta(method, path, token).map(status => assertTrue(status == harapan))
      }
    }*
  ).provide(UserRepo.live, NilaiRepo.live)
