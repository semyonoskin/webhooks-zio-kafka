package com.github.semyonoskin.webhooks.services

import com.github.semyonoskin.webhooks.models.Event
import com.github.semyonoskin.webhooks.repositories.WebhookRepo
import doobie.Transactor
import doobie.implicits._
import io.chrisdavenport.log4cats.Logger
import io.chrisdavenport.log4cats.slf4j.Slf4jLogger
import io.circe.generic.codec.DerivedAsObjectCodec.deriveCodec
import sttp.client3._
import sttp.client3.circe._
import sttp.model.{MediaType, Uri}
import zio.Task
import zio.interop.catz._

trait EventService {

  def process(event: Event): Task[Unit]

}

object EventService {

  def make(webhooks: WebhookRepo, xa: Transactor[Task], backend: SttpBackend[Task, Any]): Task[EventService] =
    Slf4jLogger.create[Task].map { implicit l =>
      new Impl(webhooks, xa, backend)
    }

  final class Impl(webhooks: WebhookRepo, xa: Transactor[Task], backend: SttpBackend[Task, Any])(implicit logger: Logger[Task]) extends EventService {

    override def process(event: Event): Task[Unit] = {
      val eventType = event.eventType
      val webhooksByEvent = webhooks.getAllByMsgType(eventType).transact(xa)
      val paths = webhooksByEvent.map { elem => elem.map { u => u.path } }
      paths.flatMap { m => Task.foreach_(m)(u => sendEvent(event, u) *> logger.info(s"sending $eventType to $u"))
      }
    }

    private def sendEvent(event: Event, path: String): Task[Unit] = {
      basicRequest
        .post(Uri.unsafeApply(path))
        .contentType(MediaType.ApplicationJson)
        .body(event)
        .send(backend)
        .flatMap { x =>
          if (x.code.isServerError) sendEvent(event, path)
          else Task.unit
        }
    }
  }
}
