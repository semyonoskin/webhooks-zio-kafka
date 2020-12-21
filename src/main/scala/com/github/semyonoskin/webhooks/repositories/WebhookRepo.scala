package com.github.semyonoskin.webhooks.repositories

import cats.implicits.toFunctorOps
import com.github.semyonoskin.webhooks.models.{Webhook, WebhookData}
import doobie.ConnectionIO
import doobie.implicits.toSqlInterpolator


trait WebhookRepo {

  def get(id: Long): ConnectionIO[Option[Webhook]]

  def put(webhookData: WebhookData): ConnectionIO[Unit]

  def remove(id: Long): ConnectionIO[Unit]

  def updateWebhook(id: Long, newEventType: String, newPath: String): ConnectionIO[Unit]

  def getAllByMsgType(msgType: String): ConnectionIO[List[Webhook]]

}

object WebhookRepo {

  def make: WebhookRepo = new DBImpl

  final class DBImpl extends WebhookRepo {

    def get(id: Long): ConnectionIO[Option[Webhook]] =
      sql"SELECT * FROM Webhooks WHERE id = $id".query[Webhook].option

    def put(webhookData: WebhookData): ConnectionIO[Unit] =
      sql"INSERT INTO Webhooks (eventtype, path) VALUES (${webhookData.eventType}, ${webhookData.path})".update.run.void

    def getAllByMsgType(eventType: String): ConnectionIO[List[Webhook]] =
      sql"SELECT * FROM Webhooks WHERE eventtype = $eventType".query[Webhook].to[List]

    def updateWebhook(id: Long, newEventType: String, newPath: String): ConnectionIO[Unit] =
      sql"UPDATE Webhooks SET eventtype = $newEventType, path = $newPath WHERE id = $id".update.run.void

    def remove(id: Long): ConnectionIO[Unit] =
      sql"DELETE FROM Webhooks WHERE id = $id".update.run.void
  }
}
