package com.github.semyonoskin.webhooks.models

import derevo.circe.magnolia.{decoder, encoder}
import derevo.derive
import io.circe.parser
import io.circe.syntax._
import zio.Task
import zio.kafka.serde.Serde

@derive(decoder, encoder)
case class Event(eventType: String)

object Event {

  val eventSerde: Serde[Any, Event] = Serde.string.inmapM[Any, Event](s => parser.parse(s)
    .flatMap(_.as[Event])
    .fold[Task[Event]](e => Task.fail(e), ev => Task(ev)))(event => Task(event.asJson.noSpaces))

}
