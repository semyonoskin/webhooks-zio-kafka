package com.github.semyonoskin.webhooks.models

final case class Webhook(id: Long, eventType: String, path: String)

final case class WebhookData(eventType: String, path: String)

