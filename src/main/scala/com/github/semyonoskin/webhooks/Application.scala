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
import com.github.semyonoskin.webhooks.configs.TransactorConfig
import com.github.semyonoskin.webhooks.configs.ServerConfig

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

  def makeBlazeServer(config: ServerConfig, service: CRUDService) = {
    val routes = new Routes(service).routes
    val app = Router("/" -> routes).orNotFound
    BlazeServerBuilder[Task](ExecutionContext.global)
      .bindHttp(config.port, config.host)
      .withHttpApp(app)
      .resource
      .toManaged
  }

  def init(pathOpt: Option[String]) =
    for {
      config <- ZManaged.fromEffect(ConfigBundle.load(pathOpt))
      httpBackend <- HttpClientZioBackend.managed()
      webhookRepo = WebhookRepo.make
      crudService = CRUDService.make(webhookRepo, makeTransactor(config.transactor))
      eventService <- EventService.make(webhookRepo, makeTransactor(config.transactor), httpBackend).toManaged_
      eventProc = EventsProcessing.make(config.consumer, eventService)
      server <- makeBlazeServer(config.server, crudService)
    } yield (eventProc, server)


  private def makeTransactor(config: TransactorConfig) = Transactor.fromDriverManager[Task](
    config.driver,
    config.jdbcConnection,
    Blocker.liftExecutionContext(ExecutionContexts.synchronous)
  )
}
