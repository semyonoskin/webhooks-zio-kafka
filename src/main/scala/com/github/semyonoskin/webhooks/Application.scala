package com.github.semyonoskin.webhooks

import cats.effect.Blocker
import com.github.semyonoskin.webhooks.configs.ConfigBundle
import com.github.semyonoskin.webhooks.http.Routes
import com.github.semyonoskin.webhooks.processes.EventsProcessing
import com.github.semyonoskin.webhooks.repositories.WebhookRepo
import com.github.semyonoskin.webhooks.services.{CRUDService, EventService}
import doobie.{ExecutionContexts, Transactor}
import org.http4s.implicits.http4sKleisliResponseSyntaxOptionT
import org.http4s.server.Router
import org.http4s.server.blaze.BlazeServerBuilder
import sttp.client3.httpclient.zio.HttpClientZioBackend
import zio._
import zio.interop.catz._

import scala.concurrent.ExecutionContext

object Application extends CatsApp {

  import implicits._

  def run(args: List[String]): URIO[ZEnv, ExitCode] = {
    init(args.headOption).use { case (process, _) =>
      process.flatMap {
        _.runConsume
      }
    }.as(ExitCode.success).orDie
  }

  def makeBlazeServer(service: CRUDService) = {
    val routes = new Routes(service).routes
    val app = Router("/" -> routes).orNotFound
    BlazeServerBuilder[Task](ExecutionContext.global)
      .bindHttp(8000, "localhost")
      .withHttpApp(app)
      .resource
      .toManaged
  }

  def init(pathOpt: Option[String]) =
    for {
      config <- ZManaged.fromEffect(ConfigBundle.load(pathOpt))
      httpBackend <- HttpClientZioBackend.managed()
      webhookRepo = WebhookRepo.make
      crudService = CRUDService.make(webhookRepo, transactor)
      eventService <- EventService.make(webhookRepo, transactor, httpBackend).toManaged_
      eventProc = EventsProcessing.make(config.consumer, eventService)
      server <- makeBlazeServer(crudService)
    } yield (eventProc, server)

  private val transactor = Transactor.fromDriverManager[Task](
    "org.postgresql.Driver",
    "jdbc:postgresql://localhost:5432/wh_db",
    Blocker.liftExecutionContext(ExecutionContexts.synchronous)
  )
}
