package com.github.semyonoskin.webhooks.services

import com.github.semyonoskin.webhooks.models.WebhookData
import com.github.semyonoskin.webhooks.repositories.WebhookRepo
import doobie.Transactor
import doobie.implicits._
import zio.Task
import zio.interop.catz._

trait CRUDService {

  def create(eventType: String, path: String): Task[Unit]

  def update(id: Long, newEventType: String, newPath: String): Task[Unit]

  def delete(id: Long): Task[Unit]

}

object CRUDService {

  def make(webhooks: WebhookRepo, xa: Transactor[Task]): CRUDService = new Impl(webhooks, xa)

  final class Impl(webhooks: WebhookRepo, xa: Transactor[Task]) extends CRUDService {

    override def create(eventType: String, path: String): Task[Unit] = {
      val webhook = WebhookData(eventType, path)
      webhooks.put(webhook).transact(xa)
    }

    override def update(id: Long, newEventType: String, newPath: String): Task[Unit] = {
      webhooks.updateWebhook(id, newEventType, newPath).transact(xa)
    }

    override def delete(id: Long): Task[Unit] = {
      webhooks.remove(id).transact(xa)
    }
  }
}
