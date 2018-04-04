package com.github.tzarouali.http4stest.app

import cats.implicits._
import cats.effect.IO
import com.github.tzarouali.http4stest.model._
import com.github.tzarouali.http4stest.repositories.UserRepository
import com.github.tzarouali.http4stest.repositories.interpreter.UserRepositoryInterpreter
import com.github.tzarouali.http4stest.services.interpreter.UserServiceInterpreter
import doobie.free.connection.ConnectionIO
import io.circe.syntax._
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl
import org.http4s.{AuthedService, EntityDecoder, EntityEncoder, HttpService}

trait UserEndpoints extends Http4sDsl[IO] with Authentication with AppTransactor {

  implicit val userSessionDataEncoder: EntityEncoder[IO, UserSessionData] = jsonEncoderOf[IO, UserSessionData]
  implicit val userLoginDecoder: EntityDecoder[IO, UserLogin] = jsonOf[IO, UserLogin]

  import UserEndpoints._

  val helloService: HttpService[IO] = authMiddleware(AuthedService[Option[User], IO] {
    case GET -> Root as user =>
      Ok(s"hello ${user.get.username}!")
  })

  val loginService: HttpService[IO] = HttpService[IO] {
    case req @ POST -> Root / "login" =>
      for {
        userLogin <- req.as[UserLogin]
        res <- userService.login(userLogin.username, userLogin.password).run(UserServiceConfig(userRepo)).transact()
      } yield {
        res match {
          case Some(value) =>
            value match {
              case Right(r) =>
                Ok(r.asJson).unsafeRunSync()

              case Left(l) =>
                BadRequest(l.err).unsafeRunSync()
            }

          case None =>
            InternalServerError("Error executing the log-in").unsafeRunSync()
        }
      }
  }

}

object UserEndpoints extends UserEndpoints {

  val userRepo = UserRepositoryInterpreter
  val userService = UserServiceInterpreter

  final case class UserServiceConfig(userRepo: UserRepository[ConnectionIO])

  def endpoints() : HttpService[IO] = {
    helloService <+> loginService
  }


}
