package siak

import zio.*
import zio.http.*

object Akses:

  // Autentikasi: "kamu siapa?". Membaca Bearer token lalu menyediakan User ke handler.
  // Token tidak ada atau tidak dikenal berarti 401.
  val autentikasi: HandlerAspect[UserRepo, User] =
    HandlerAspect.customAuthProvidingZIO(
      (req: Request) =>
        req.header(Header.Authorization) match
          case Some(Header.Authorization.Bearer(token)) =>
            ZIO.serviceWithZIO[UserRepo](_.cariDariToken(token.stringValue))
          case _ => ZIO.none,
      Headers(Header.WWWAuthenticate.Bearer(realm = "SIAK"))
    )

  // Otorisasi: "kamu boleh apa?". Membaca User dari context, lalu bertanya ke Rbac.
  // Tidak punya izin berarti 403.
  def butuh(aksi: Aksi): HandlerAspect[User, Unit] =
    HandlerAspect.interceptIncomingHandler(
      Handler.fromFunctionZIO[Request] { req =>
        ZIO.serviceWithZIO[User] { user =>
          if Rbac.boleh(user.role, aksi) then ZIO.succeed((req, ()))
          else ZIO.fail(Response.forbidden(s"${user.role} tidak punya izin $aksi"))
        }
      }
    )
