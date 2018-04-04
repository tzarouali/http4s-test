package com.github.tzarouali.http4stest.model

import io.circe._
import io.circe.generic.semiauto._

final case class Password(p: String) extends AnyVal

object Password {
  implicit val passwordDecoder: Decoder[Password] = deriveDecoder
}
