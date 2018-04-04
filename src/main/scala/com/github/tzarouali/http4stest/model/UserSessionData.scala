package com.github.tzarouali.http4stest.model

import io.circe._
import io.circe.generic.semiauto._

final case class UserSessionData(userId: UserId, token: Token)

object UserSessionData {
  implicit val userSessionDataEncoder: Encoder[UserSessionData] = deriveEncoder
  implicit val userSessionDataDecoder: Decoder[UserSessionData] = deriveDecoder
}
