package com.github.semyonoskin.webhooks.http

import com.github.semyonoskin.webhooks.services.CRUDService
import io.circe.generic.codec.DerivedAsObjectCodec.deriveCodec
import org.http4s.circe.CirceEntityCodec.circeEntityEncoder
import org.http4s.circe.jsonOf
import org.http4s.dsl.Http4sDsl
import org.http4s.{EntityDecoder, HttpRoutes}
import zio.Task
import zio.interop.catz._


final case class CreateWebhookRequest(eventType: String, path: String)

final case class UpdateWebhookRequest(id: Long, eventType: String, path: String)

final case class DeleteWebhookRequest(id: Long)


final class Routes(CRUDService: CRUDService) {

  implicit val createWhDecoder: EntityDecoder[Task, CreateWebhookRequest] = jsonOf[Task, CreateWebhookRequest]

  implicit val updateWhDecoder: EntityDecoder[Task, UpdateWebhookRequest] = jsonOf[Task, UpdateWebhookRequest]

  implicit val deleteWhDecoder: EntityDecoder[Task, DeleteWebhookRequest] = jsonOf[Task, DeleteWebhookRequest]

  val dsl: Http4sDsl[Task] = Http4sDsl[Task]
  import dsl._

  val routes: HttpRoutes[Task] = HttpRoutes.of[Task] {

    case req @ POST -> Root / "create" =>
      for {
        webhook <- req.as[CreateWebhookRequest]
        resp <- Ok(CRUDService.create(webhook.eventType, webhook.path))
      }yield resp

    case req @ POST -> Root / "update" =>
      for {
        request <- req.as[UpdateWebhookRequest]
        resp <- Ok(CRUDService.update(request.id, request.eventType, request.path ))
      } yield resp

    case req @ POST -> Root / "delete" =>
      for {
        request <- req.as[DeleteWebhookRequest]
        resp <- Ok(CRUDService.delete(request.id))
      } yield resp
  }
}
