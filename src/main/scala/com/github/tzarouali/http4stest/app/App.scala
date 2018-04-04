package com.github.tzarouali.http4stest.app

import cats.effect.IO
import fs2.StreamApp
import org.http4s.server.blaze.BlazeBuilder

import scala.concurrent.ExecutionContext.Implicits.global

object App extends StreamApp[IO] with UserEndpoints {


  def stream(args: List[String], requestShutdown: IO[Unit]) =
    BlazeBuilder[IO]
      .bindHttp(8080, "0.0.0.0")
      .mountService(helloService, "/")
      .mountService(loginService, "/")
      .serve
}
