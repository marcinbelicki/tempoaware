package pl.belicki.tempomem.util

import play.api.libs.ws.StandaloneWSRequest

import scala.concurrent.{ExecutionContext, Future}

object ResponseHandler {

  implicit class FlatMapResponse(future: Future[StandaloneWSRequest#Response]) {
    def flatMapStatus(pf: PartialFunction[Int, StandaloneWSRequest#Response => Future[StandaloneWSRequest#Response]])(implicit ec: ExecutionContext): Future[StandaloneWSRequest#Response] =
      future.flatMap {
        response =>
          pf
            .lift(response.status)
            .map(_.apply(response))
            .getOrElse(Future.failed(new IllegalStateException(s"Unexpected status code ${response.status} of $response.")))
      }

    def flatMapUnauthorizedAndNotFound(url: String)(implicit ec: ExecutionContext): Future[StandaloneWSRequest#Response] =
      flatMapStatus(unauthorizedAndNotFound(url))
  }

  def unauthorizedAndNotFound(url: String): PartialFunction[Int, StandaloneWSRequest#Response => Future[StandaloneWSRequest#Response]] = {
    case 200 => Future.successful
    case 401 => _ => Future.failed(new IllegalStateException(s"Request was unauthorized - status code 401 for url $url"))
    case 404 => _ => Future.failed(new IllegalStateException(s"Resource not found - status code 404 for url $url"))
  }

}
