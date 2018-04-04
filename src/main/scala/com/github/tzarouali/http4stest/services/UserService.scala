package com.github.tzarouali.http4stest.services

import cats.data.{Kleisli, Reader}
import com.github.tzarouali.http4stest.app.UserEndpoints.UserServiceConfig
import com.github.tzarouali.http4stest.model._

trait UserService[F[_], G] {

  def findUser(token: Token): Reader[UserServiceConfig, Kleisli[F, G, Either[UserNotFoundError, User]]]
  def login(username: Username, password: Password): Reader[UserServiceConfig, Kleisli[F, G, Either[LoginError, UserSessionData]]]

}
