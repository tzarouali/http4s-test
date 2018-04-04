package com.github.tzarouali.http4stest.app

import cats.data.{Kleisli, OptionT}
import cats.effect.IO
import cats.implicits._
import com.github.tzarouali.http4stest.app.UserEndpoints.UserServiceConfig
import com.github.tzarouali.http4stest.model._
import com.github.tzarouali.http4stest.repositories.interpreter.UserRepositoryInterpreter
import com.github.tzarouali.http4stest.services.interpreter.UserServiceInterpreter
import org.http4s.dsl.Http4sDsl
import org.http4s.server.AuthMiddleware
import org.http4s.util.CaseInsensitiveString
import org.http4s.{AuthedService, Request}

trait Authentication extends Http4sDsl[IO] with AppTransactor {

  val userRepo = UserRepositoryInterpreter
  val userService = UserServiceInterpreter

  def retrieveUser: Kleisli[IO, String, Option[User]] = Kleisli(token => {
    userService.findUser(Token(token)).run(UserServiceConfig(userRepo)).transact() map {
      case Some(u) =>
        u.toOption
      case None =>
        None
    }
  })

  val authUser: Kleisli[IO, Request[IO], Either[String, Option[User]]] = Kleisli({ request =>
    val message = for {
      header <- request.headers.get(CaseInsensitiveString("token")).toRight("Couldn't find the token header")
      token <- Right(header.value)
    } yield token
    message.traverse(retrieveUser.run)
  })

  val onFailure: AuthedService[String, IO] = Kleisli(req => OptionT.liftF(Forbidden(req.authInfo)))

  val authMiddleware: AuthMiddleware[IO, Option[User]] = AuthMiddleware(authUser, onFailure)

}
