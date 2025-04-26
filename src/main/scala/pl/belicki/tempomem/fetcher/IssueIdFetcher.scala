package pl.belicki.tempomem.fetcher

import play.api.libs.json.{JsObject, JsString, JsValue}
import play.api.libs.ws.JsonBodyReadables.readableAsJson
import play.api.libs.ws.{StandaloneWSClient, WSAuthScheme}

import scala.concurrent.{ExecutionContext, Future}

class IssueIdFetcher(
                      jiraToken: String,
                      jiraUser: String,
                      jiraUrl: String,
                    )
                    (implicit ec: ExecutionContext, wsClient: StandaloneWSClient) extends Fetcher[String, Long] {

  override def fetchFuture(key: String): Future[Long] =
    wsClient
      .url(s"$jiraUrl/rest/api/3/issue/$key")
      .withAuth(jiraUser, jiraToken, WSAuthScheme.BASIC)
      .get()
      .map(_.body[JsValue].as[JsObject].value("id").as[JsString].value.toLong)
}
