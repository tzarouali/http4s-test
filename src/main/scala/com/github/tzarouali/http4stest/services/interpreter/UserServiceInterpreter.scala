package com.github.tzarouali.http4stest.services.interpreter

import java.math.BigInteger
import java.security.SecureRandom
import java.time.Instant
import java.time.temporal.ChronoUnit

import cats.data.{Kleisli, Reader}
import cats.effect.IO
import com.github.tzarouali.http4stest.DbConnection
import com.github.tzarouali.http4stest.app.UserEndpoints.UserServiceConfig
import com.github.tzarouali.http4stest.model._
import com.github.tzarouali.http4stest.services.UserService
import doobie._
import tsec.common._
import tsec.messagedigests._
import tsec.messagedigests.imports._

trait UserServiceInterpreter extends UserService[IO, DbConnection] {

  import UserServiceInterpreter._

  override def findUser(token: Token): Reader[UserServiceConfig, Kleisli[IO, DbConnection, Either[UserNotFoundError, User]]] =
    Reader { config =>
      val interpreter = KleisliInterpreter[IO].ConnectionInterpreter
      config.userRepo.findUser(token).value.foldMap(interpreter).map(_.toRight(UserNotFoundError()))
    }

  override def login(username: Username, password: Password): Reader[UserServiceConfig, Kleisli[IO, DbConnection, Either[LoginError, UserSessionData]]] =
    Reader { config =>

      val interpreter = KleisliInterpreter[IO].ConnectionInterpreter

      val connToUser = config.userRepo.findUser(username).value.foldMap(interpreter)

      val connToUserSessionData = connToUser
        .andThen(Kleisli[IO, Option[User], (Boolean, Option[UserId])] { // check password
          case Some(u) if passwordsMatch(u, password) =>
            IO((true, Some(u.id)))
          case _ =>
            IO((false, None))
        })
        .flatMap { // update token and expiration
          case (true, Some(u)) =>
            val expirationTime = Instant.now().plus(defaultExpirationTime, defaultExpirationUnit)
            val newToken = Token(generateToken())
            config.userRepo.updateExpirationAndToken(u, expirationTime, newToken).foldMap(interpreter).map((_, Option(u), Option(newToken)))
          case _ =>
            Kleisli.liftF[IO, DbConnection, (Boolean, Option[UserId], Option[Token])](IO((false, None, None)))
        }
        .map { // return session data
          case (true, Some(id), Some(t)) =>
            IO(Some(UserSessionData(id, t)))
          case _ =>
            IO(None)
        }

      connToUserSessionData.flatMapF(_.map(_.toRight(LoginError("Error trying to log in. Verify your credentials."))))
    }

}

object UserServiceInterpreter extends UserServiceInterpreter {

  private val defaultExpirationTime = 1L
  private val defaultExpirationUnit = ChronoUnit.HOURS

//  private val tokenNotExpired: Instant => Boolean =
//    expirationDate => Instant.now().isBefore(expirationDate)

  private def generateToken(): String = {
    val random = new SecureRandom()
    new BigInteger(130, random).toString(32)
  }

  private def passwordsMatch(user: User, password: Password): Boolean = {
    implicit val pickler: CryptoPickler[String] = CryptoPickler.stringPickle[UTF8]
    val hashedPass = password.p.pickleAndHash[SHA256].toHexString
    user.password.p == hashedPass
  }

}
