package com.github.tzarouali.http4stest.model

import io.circe._
import io.circe.generic.semiauto._

final case class UserLogin(username: Username, password: Password)

object UserLogin {
  implicit val userLoginDecoder: Decoder[UserLogin] = deriveDecoder
}
