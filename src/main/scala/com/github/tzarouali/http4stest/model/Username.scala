package com.github.tzarouali.http4stest.model

import io.circe._
import io.circe.generic.semiauto._

final case class Username(n: String) extends AnyVal

object Username {
  implicit val usernameDecoder: Decoder[Username] = deriveDecoder
}
