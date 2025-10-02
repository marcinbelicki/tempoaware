package pl.belicki.tempomem.fetcher

import pl.belicki.tempomem.fetcher.AccountIdFetcher.Info
import pl.belicki.tempomem.util.ResponseHandler.FlatMapResponse
import play.api.libs.json.{JsObject, JsString, JsValue}
import play.api.libs.ws.JsonBodyReadables.readableAsJson
import play.api.libs.ws.{StandaloneWSClient, WSAuthScheme}

import java.time.ZoneId
import scala.concurrent.{ExecutionContext, Future}

class AccountIdFetcher(
    jiraToken: String,
    jiraUser: String,
    jiraUrl: String
)(implicit ec: ExecutionContext, wsClient: StandaloneWSClient)
    extends Fetcher[Unit, Info] {

  private val request = wsClient
    .url(s"$jiraUrl/rest/api/3/myself")
    .withAuth(jiraUser, jiraToken, WSAuthScheme.BASIC)

  override def fetchFuture(key: Unit): Future[Info] =
    request
      .get()
      .flatMapUnauthorizedAndNotFound(request.url)
      .map { response =>
        response.status
        val value = response.body[JsValue].as[JsObject].value

        Info(
          accountId = value("accountId").as[JsString].value,
          zoneId = ZoneId.of(value("timeZone").as[JsString].value)
        )

      }
}

object AccountIdFetcher {
  case class Info(
      accountId: String,
      zoneId: ZoneId
  )
}
