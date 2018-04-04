package com.github.tzarouali.http4stest.model

import java.time.Instant

import io.circe._
import io.circe.generic.semiauto._
import io.circe.java8.time._

final case class User(id: UserId,
                      username: Username,
                      password: Password,
                      token: Option[Token],
                      tokenExpiration: Option[Instant])


object User {
  implicit val userDecoder: Decoder[User] = deriveDecoder
}
