package com.github.semyonoskin.webhooks

import com.github.semyonoskin.webhooks.models.Event
import zio.kafka.producer.{Producer, ProducerSettings}
import zio.kafka.serde.Serde
import zio._
import zio.interop.catz._

object ProduceApp extends CatsApp {

  val producerSettings: ProducerSettings = ProducerSettings(List("localhost:9092"))

  val producer: ZManaged[Any, Throwable, Producer.Service[Any, String, Event]] =
    Producer.make(producerSettings, Serde.string, Event.eventSerde)

  val events = List(Event("event1"), Event("event2"), Event("event3"))

  def run(args: List[String]): URIO[ZEnv, ExitCode] = {
    producer.use { prod =>
      RIO.foreach(events)(event => {
        prod.produce("topic", "key", event)
      })
    }.as(ExitCode.success).orDie
  }
}
