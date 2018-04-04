package com.github.tzarouali.http4stest.repositories

import java.time.Instant

import cats.data.OptionT
import com.github.tzarouali.http4stest.model.{Token, User, UserId, Username}

trait UserRepository[F[_]] extends BaseRepository {
  def findUser(token: Token): OptionT[F, User]
  def findUser(username: Username): OptionT[F, User]
  def updateExpirationAndToken(userId: UserId, expiration: Instant, token: Token): F[Boolean]
}
