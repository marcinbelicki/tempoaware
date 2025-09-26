package pl.belicki.tempoaware.util

import play.api.libs.ws.DefaultBodyReadables.readableAsString
import play.api.libs.ws.StandaloneWSRequest

import scala.concurrent.{ExecutionContext, Future}

object ResponseHandler {

  implicit class FlatMapResponse(future: Future[StandaloneWSRequest#Response]) {
    def flatMapStatus(
        pf: PartialFunction[Int, StandaloneWSRequest#Response => Future[
          StandaloneWSRequest#Response
        ]]
    )(implicit ec: ExecutionContext): Future[StandaloneWSRequest#Response] =
      future.flatMap { response =>
        pf
          .lift(response.status)
          .map(_.apply(response))
          .getOrElse(
            Future.failed(
              new IllegalStateException(
                s"Unexpected status code ${response.status} with body ${response.body[String]}."
              )
            )
          )
      }

    def flatMapUnauthorizedAndNotFound(url: String)(implicit
        ec: ExecutionContext
    ): Future[StandaloneWSRequest#Response] =
      flatMapStatus(unauthorizedAndNotFound(url))
  }

  def unauthorizedAndNotFound(
      url: String
  ): PartialFunction[Int, StandaloneWSRequest#Response => Future[
    StandaloneWSRequest#Response
  ]] = {
    case 200 => Future.successful
    case 401 =>
      response =>
        Future.failed(
          new IllegalStateException(
            s"Request was unauthorized - status code 401 for url $url; response body: ${response.body[String]}"
          )
        )
    case 404 =>
      response =>
        Future.failed(
          new IllegalStateException(
            s"Resource not found - status code 404 for url $url; response body: ${response.body[String]}"
          )
        )
  }

}
