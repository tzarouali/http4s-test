package com.github.tzarouali.http4stest.repositories.interpreter

import java.time.Instant

import cats.data.OptionT
import com.github.tzarouali.http4stest.model._
import com.github.tzarouali.http4stest.repositories.UserRepository
import doobie._
import doobie.implicits._

trait UserRepositoryInterpreter extends UserRepository[ConnectionIO] {

  override def findUser(token: Token): OptionT[ConnectionIO, User] =
    OptionT(
      sql"""select id, username, password, token, token_expiration
            from apiuser
            where token = $token
         """
        .query[User]
        .option
    )

  override def findUser(username: Username): OptionT[ConnectionIO, User] =
    OptionT(
      sql"""select id, username, password, token, token_expiration
            from apiuser
            where username = $username
         """
        .query[User]
        .option
    )

  override def updateExpirationAndToken(userId: UserId, expiration: Instant, token: Token): ConnectionIO[Boolean] =
    sql"""update apiuser
          set token = ${token.t},
              token_expiration = $expiration
          where id = ${userId.id}
         """
      .update
      .run.map(_ > 0)

}

object UserRepositoryInterpreter extends UserRepositoryInterpreter
