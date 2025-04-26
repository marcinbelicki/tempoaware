package pl.belicki.tempomem.fetcher

import org.jline.reader.Candidate
import pl.belicki.tempomem.fetcher.IssueKeyFetcher.Info
import play.api.libs.json.{JsObject, JsString, JsValue}
import play.api.libs.ws.JsonBodyReadables.readableAsJson
import play.api.libs.ws.{StandaloneWSClient, WSAuthScheme}

import scala.concurrent.{ExecutionContext, Future}

class IssueKeyFetcher(
                       jiraToken: String,
                       jiraUser: String,
                       jiraUrl: String,
                     )
                     (implicit ec: ExecutionContext, wsClient: StandaloneWSClient) extends Fetcher[Long, Info] {

  override def fetchFuture(key: Long): Future[Info] =
    wsClient
      .url(s"$jiraUrl/rest/api/3/issue/$key")
      .withAuth(jiraUser, jiraToken, WSAuthScheme.BASIC)
      .get()
      .map {
        response =>
          val responseObject = response.body[JsValue].as[JsObject].value
          val key = responseObject("key").as[JsString].value
          val summary = responseObject("fields").as[JsObject].value("summary").as[JsString].value

          Info(
            key, summary
          )

      }

}

object IssueKeyFetcher {
  case class Info(
                   key: String,
                   summary: String
                 )
}
