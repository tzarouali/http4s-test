package com.github.tzarouali.http4stest.model

import io.circe._
import io.circe.generic.semiauto._

final case class Token(t: String) extends AnyVal

object Token {
  implicit val tokenEncoder: Encoder[Token] = deriveEncoder
  implicit val tokenDecoder: Decoder[Token] = deriveDecoder
}
