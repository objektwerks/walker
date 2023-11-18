package walker

import com.github.plokhotnyuk.jsoniter_scala.core.*
import com.typesafe.scalalogging.LazyLogging

import io.helidon.webclient.api.WebClient

import java.net.URI
import java.net.http.{HttpClient, HttpRequest, HttpResponse}
import java.net.http.HttpResponse.BodyHandlers
import java.time.Duration
import java.time.temporal.ChronoUnit.SECONDS
import java.util.concurrent.Executors

import scalafx.application.Platform
import scala.concurrent.ExecutionContext
import scala.jdk.FutureConverters.*

final class Fetcher(context: Context) extends LazyLogging:
  private val url = context.url
  private val connectError = context.errorServer
  private val client = WebClient
    .builder
    .baseUri(url)
    .addHeader("Content-Type", "application/json; charset=UTF-8")
    .addHeader("Accept", "application/json")
    .build

  logger.info(s"*** Fetcher url: $url")

  def fetchAsync(command: Command,
                 handler: Event => Unit): Unit =
    logger.info(s"*** Fetcher command: $command")
    val commandJson = writeToString[Command](command)


    val eventJson = client
      .post(endpoint)
      .submit(commandJson, classOf[String])
      .entity

        val event = readFromString[Event](eventJson)
        logger.info(s"*** Fetcher event: $event")
        Platform.runLater(handler(event))
      }.recover {
        case error: Exception =>
          val fault = Fault(
            if error.getMessage == null then connectError
            else error.getMessage
          )
          logger.error(s"Fetcher fault: $fault")
          handler(fault)
      }