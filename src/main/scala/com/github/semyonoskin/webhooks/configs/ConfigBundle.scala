package com.github.semyonoskin.webhooks.configs

import pureconfig._
import pureconfig.generic.semiauto.deriveReader
import pureconfig.module.catseffect.syntax._
import zio.Task
import zio.interop.catz._

import scala.concurrent.duration.FiniteDuration


case class ConsumerConfig(bootstrapServers: List[String],
                          groupId: String,
                          clientId: String,
                          closeTimeout: FiniteDuration,
                          topicId: String)

object ConsumerConfig {

  implicit val configReader: ConfigReader[ConsumerConfig] = deriveReader[ConsumerConfig]

}

case class ConfigBundle(consumer: ConsumerConfig)

object ConfigBundle {

  implicit val bundleReader: ConfigReader[ConfigBundle] = deriveReader[ConfigBundle]

  def load(pathOpt: Option[String]): Task[ConfigBundle] = pathOpt match {
    case Some(conf) => ConfigSource.file(conf).loadF[Task, ConfigBundle]
    case None => ConfigSource.default.loadF[Task, ConfigBundle]
  }
}
