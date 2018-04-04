package com.github.tzarouali.http4stest.app

import cats.Id
import cats.data.Kleisli
import cats.effect.IO
import com.github.tzarouali.http4stest.DbConnection
import doobie.hikari._

trait AppTransactor {

  private[AppTransactor] final val tx = HikariTransactor.newHikariTransactor[IO](
    "org.postgresql.Driver",
    "jdbc:postgresql:http4s_test_db",
    "http4s_test_user",
    "http4s_test_user"
  )

  implicit class TransactedConn[A](k: Id[Kleisli[IO, DbConnection, A]]) {
    def transact(): IO[Option[A]] = {
      val dsToConnIO = tx.unsafeRunSync().connect
      val connIO = dsToConnIO(tx.unsafeRunSync().kernel)
      connIO.map(conn => {
        conn.setAutoCommit(false)
        k.run(conn).attempt.unsafeRunSync() match {
          case Right(v) =>
            conn.commit()
            Some(v)
          case Left(err) =>
            err.printStackTrace()
            conn.rollback()
            conn.close()
            None
        }
      })
    }
  }

}

