package pl.belicki.tempomem.fetcher

import pl.belicki.tempomem.fetcher.AccountIdFetcher.Info
import play.api.libs.json.{JsObject, JsString, JsValue}
import play.api.libs.ws.JsonBodyReadables.readableAsJson
import play.api.libs.ws.{StandaloneWSClient, WSAuthScheme}

import java.time.ZoneId
import scala.concurrent.{ExecutionContext, Future}

class AccountIdFetcher(
                        jiraToken: String,
                        jiraUser: String,
                        jiraUrl: String,
                      )(implicit ec: ExecutionContext, wsClient: StandaloneWSClient) extends Fetcher[Unit, Info] {

  override def fetchFuture(key: Unit): Future[Info] =
    wsClient
      .url(s"$jiraUrl/rest/api/3/myself")
      .withAuth(jiraUser, jiraToken, WSAuthScheme.BASIC)
      .get()
      .map {
        response =>
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
