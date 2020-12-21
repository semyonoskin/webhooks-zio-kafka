package com.github.semyonoskin.webhooks.processes

import com.github.semyonoskin.webhooks.configs.ConsumerConfig
import com.github.semyonoskin.webhooks.models.Event
import com.github.semyonoskin.webhooks.services.EventService
import io.chrisdavenport.log4cats.Logger
import io.chrisdavenport.log4cats.slf4j.Slf4jLogger
import zio._
import zio.blocking.Blocking
import zio.clock.Clock
import zio.duration._
import zio.interop.catz._
import zio.kafka.consumer.{Consumer, ConsumerSettings, _}
import zio.kafka.serde._

trait EventsProcessing {

  def runConsume: ZIO[Blocking with Clock, Throwable, Unit]

}

object EventsProcessing {

  def make(config: ConsumerConfig, eventService: EventService): Task[EventsProcessing] =
    Slf4jLogger.create[Task].map { implicit l =>
      val settings: ConsumerSettings =
        ConsumerSettings(config.bootstrapServers)
          .withGroupId(config.groupId)
          .withClientId(config.clientId)
          .withCloseTimeout(Duration.fromScala(config.closeTimeout))
      val subscription: Subscription = Subscription.topics("topic")
      new Impl(settings, subscription, eventService)
    }

  final class Impl(settings: ConsumerSettings, subscription: Subscription, eventService: EventService)(implicit logger: Logger[Task]) extends EventsProcessing {

    def runConsume: ZIO[Blocking with Clock, Throwable, Unit] = {
      Consumer.consumeWith(settings, subscription, Serde.string, Event.eventSerde) { case (_, value) =>
        eventService.process(value).fold(e => logger.error(e)(e.getMessage), identity)
      }
    }
  }
}