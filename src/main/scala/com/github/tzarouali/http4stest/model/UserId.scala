package com.github.tzarouali.http4stest.model

import io.circe._
import io.circe.generic.semiauto._

final case class UserId(id: Long) extends AnyVal

object UserId {
  implicit val userIdEncoder: Encoder[UserId] = deriveEncoder
  implicit val userIdDecoder: Decoder[UserId] = deriveDecoder
}
