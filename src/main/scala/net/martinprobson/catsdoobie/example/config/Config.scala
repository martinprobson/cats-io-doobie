package net.martinprobson.catsdoobie.example.config

import pureconfig._
import pureconfig.generic.auto._
import pureconfig.module.catseffect.syntax._
import cats.effect.IO

case class Config(threads: Int, jdbc: Jdbc)

case class Jdbc(driverClassName: String, url: String, user: String, password: String)

object Config {

  def loadConfig: IO[Config] = ConfigSource.default.loadF[IO, Config]()
}
