package com.github.tzarouali.http4stest.model

sealed trait ApplicationError extends Product with Serializable

final case class UserNotFoundError() extends ApplicationError

final case class LoginError(err: String) extends ApplicationError

final case class LogoutError(err: String) extends ApplicationError
