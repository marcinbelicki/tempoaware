package pl.belicki.tempomem.command

import cats.data.{IorT, NonEmptyChain}
import cats.implicits.toTraverseOps
import org.jline.reader.Candidate
import pl.belicki.tempomem.command.aggregator.UndoAggregator
import pl.belicki.tempomem.command.response.{DeleteLogResponse, LogResponse, Response, UpdateLogResponse}
import pl.belicki.tempomem.fetcher.{AccountIdFetcher, CachingFetcher, IssueIdFetcher, IssueKeyFetcher}
import pl.belicki.tempomem.info.Info
import pl.belicki.tempomem.info.Info._
import pl.belicki.tempomem.util.ResponseHandler.FlatMapResponse
import play.api.libs.json._
import play.api.libs.ws.JsonBodyReadables.readableAsJson
import play.api.libs.ws.JsonBodyWritables.writeableOf_JsValue
import play.api.libs.ws.{StandaloneWSClient, StandaloneWSRequest}

import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.time.{Instant, LocalDate, LocalDateTime, ZoneId, ZonedDateTime}
import scala.concurrent.duration.{Duration, DurationInt}
import scala.concurrent.{ExecutionContext, Future}

class CommandConnector(
                        tempoToken: String,
                        jiraToken: String,
                        jiraUser: String,
                        jiraUrl: String,
                        undoAggregator: UndoAggregator
                      )(implicit ec: ExecutionContext, wsClient: StandaloneWSClient) {


  private val tempoAuth = "Authorization" -> s"Bearer $tempoToken"

  val issueIdFetcher: IssueIdFetcher = new IssueIdFetcher(
    jiraToken = jiraToken,
    jiraUser = jiraUser,
    jiraUrl = jiraUrl
  ) with CachingFetcher[String, Long] {
    override implicit protected def ec: ExecutionContext = CommandConnector.this.ec

    override protected val ttl: Option[Duration] = Some(30.seconds)
  }

  private val issueCandidatesFetcher: IssueKeyFetcher = new IssueKeyFetcher(
    jiraToken = jiraToken,
    jiraUser = jiraUser,
    jiraUrl = jiraUrl
  ) with CachingFetcher[Long, IssueKeyFetcher.Info] {
    override implicit protected def ec: ExecutionContext = CommandConnector.this.ec

    override protected val ttl: Option[Duration] = Some(30.seconds)
  }

  val accountIdFetcher: AccountIdFetcher = new AccountIdFetcher(
    jiraToken = jiraToken,
    jiraUser = jiraUser,
    jiraUrl = jiraUrl
  ) with CachingFetcher[Unit, AccountIdFetcher.Info] {
    override implicit protected def ec: ExecutionContext = CommandConnector.this.ec

    override protected val ttl: Option[Duration] = Some(1.day)
  }

  private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss")

  private val worklogsUrl = "https://api.tempo.io/4/worklogs"
  private val worklogStatusCodeHandling: PartialFunction[Int, StandaloneWSRequest#Response => Future[StandaloneWSRequest#Response]] = {
    case 200 => Future.successful
    case 404 => _ => Future.failed(new IllegalStateException(s"Authenticated user is missing permission to fulfill the request for url $worklogsUrl"))
  }

  def log(logCommand: LogCommand): Future[Response] =
    wsClient
      .url(worklogsUrl)
      .withHttpHeaders(tempoAuth)
      .post(
        JsObject(
          Seq(
            "authorAccountId" -> JsString(logCommand.authorAccountId),
            "issueId" -> JsNumber(logCommand.issueId),
            "startDate" -> JsString(logCommand.startDate.toString),
            "startTime" -> JsString(timeFormatter.format(logCommand.startTime)),
            "timeSpentSeconds" -> JsNumber(logCommand.timeSpentSeconds),
            "description" -> JsString(logCommand.description)
          )
        )
      )
      .map(_.body[JsValue].as[JsObject])
      .map {
        jsObject =>
          val id = jsObject.value("tempoWorklogId").as[JsNumber].value.toLongExact
          val deleteLogCommand = DeleteLogCommand(id)
          undoAggregator.addUndo(deleteLogCommand)
          LogResponse(id)
      }

  def deleteLog(deleteLogCommand: DeleteLogCommand): Future[Response] =
    wsClient
      .url(s"$worklogsUrl/${deleteLogCommand.id}")
      .withHttpHeaders(tempoAuth)
      .delete()
      .map(_ => DeleteLogResponse(deleteLogCommand.id))



  def getWorklogsQuery(from: LocalDate, to: LocalDate, orderBy: String): Future[StandaloneWSRequest#Response] =
    wsClient
      .url(worklogsUrl)
      .withHttpHeaders(tempoAuth)
      .withQueryStringParameters(
        "from" -> from.toString,
        "to" -> to.toString,
        "orderBy" -> orderBy,
      )
      .get()
      .flatMapStatus(worklogStatusCodeHandling)


  def getWorklogsQuery(from: LocalDate, to: LocalDate, orderBy: String, limit: Int): Future[StandaloneWSRequest#Response] =
    wsClient
      .url(worklogsUrl)
      .withHttpHeaders(tempoAuth)
      .withQueryStringParameters(
        "from" -> from.toString,
        "to" -> to.toString,
        "orderBy" -> orderBy,
        "limit" -> limit.toString
      )
      .get()
      .flatMapStatus(worklogStatusCodeHandling)

  def getWorklogsObject(response: StandaloneWSRequest#Response): List[JsObject] =
    response.body[JsValue]
      .as[JsObject]
      .value("results")
      .as[JsArray]
      .value
      .map(_.as[JsObject])
      .toList

  private def getLatestWorklogs(from: LocalDate, to: LocalDate, orderBy: String, limit: Int): IorTNec[List[JsObject]] =
    getWorklogsQuery(from, to, orderBy, limit).map(getWorklogsObject)


  private def getLatestWorklogs(from: LocalDate, to: LocalDate, orderBy: String): IorTNec[List[JsObject]] =
    getWorklogsQuery(from, to, orderBy).map(getWorklogsObject)

  def getLatestWorklogsCandidates: IorTNec[Seq[Candidate]] =
    for {
      accountInfo <- accountIdFetcher.fetch()
      today = LocalDate.now(accountInfo.zoneId)
      weekBefore = today.minusWeeks(1)
      latestWorklogs <- getLatestWorklogs(weekBefore, today, "START_DATE_TIME")
      issuesIds = latestWorklogs
        .map(_.value("issue").as[JsObject].value("id").as[JsNumber].value.toLongExact)
        .zipWithIndex
        .groupMapReduce(_._1)(_._2)(Math.min)
        .toList
      candidates <- issuesIds.traverse {
        case (id, sortingInt) =>
          for {
            info <- issueCandidatesFetcher.fetch(id)
          } yield new Candidate(
            info.key,
            info.key,
            null,
            info.summary,
            null,
            null,
            true,
            sortingInt
          )
      }
    } yield candidates


  private def getLatestWorklogForDate(localDate: LocalDate): IorTNec[JsObject] =
    getLatestWorklogs(localDate, localDate, "START_DATE_TIME", 1)
      .flatMap {
        case latestWorklog :: Nil => IorT.pure(latestWorklog)
        case _ => IorT.leftT(NonEmptyChain.one(Info(s"Could not find latest worklog for the date $localDate.", InfoType.Error)))
      }

  private def fetchZoneId: IorTNec[ZoneId] =
    for {
      accountInfo <- accountIdFetcher.fetch()
    } yield accountInfo.zoneId

  private def getLatestWorklogToday(zoneId: ZoneId): IorTNec[JsObject] = {
    val localDate = LocalDate.now(zoneId)
    for {
      latestWorklog <- getLatestWorklogForDate(localDate)
    } yield latestWorklog
  }

  def getLatestWorklogEndDateTime: IorTNec[LocalDateTime] = {
    for {
      zoneId <- fetchZoneId
      latestWorklog <- getLatestWorklogToday(zoneId)
      valueMap = latestWorklog.value
    } yield {


      val timeSpentSeconds = valueMap("timeSpentSeconds").as[JsNumber].value.toLongExact
      val startDateTime = valueMap("startDateTimeUtc").as[JsString].value
      Instant.parse(startDateTime).plusSeconds(timeSpentSeconds).atZone(zoneId).toLocalDateTime
    }
  }

  private val fieldsForUpdate = Set("startDate", "description", "startTime")

  def getUpdateLastToNowCommand: IorTNec[Command] = {
    import cats.Invariant.catsInstancesForFuture
    for {
      accountInfo <- accountIdFetcher.fetch()
      zoneId = accountInfo.zoneId
      authorAccountId = accountInfo.accountId
      latestWorklog <- getLatestWorklogToday(zoneId)
      valueMap = latestWorklog.value
      id = valueMap("tempoWorklogId").as[JsNumber].value.toLongExact
      startDateTime = Instant.parse(valueMap("startDateTimeUtc").as[JsString].value).atZone(zoneId)
      endDateTime = ZonedDateTime.now(zoneId).truncatedTo(ChronoUnit.MINUTES)
      timeSpent = java.time.Duration.between(startDateTime, endDateTime)
      timeSpentSeconds <- if (timeSpent.isPositive) IorT.pure[Future, NonEmptyChain[Info]](timeSpent.toSeconds)
      else IorT.leftT(NonEmptyChain.one(Info("Start date time is equal or after end date time.", InfoType.Error)))
      updatedObject = JsObject(
        valueMap
          .view
          .filterKeys(fieldsForUpdate)
          .toMap
          .updated("timeSpentSeconds", JsNumber(timeSpentSeconds))
          .updated("authorAccountId", JsString(authorAccountId))
      )
    } yield UpdateLogCommand(
      id = id,
      updated = updatedObject
    )
  }


  def updateLog(updateLogCommand: UpdateLogCommand): IorTNec[Response] =
    for {
      response <- wsClient
        .url(s"https://api.tempo.io/4/worklogs/${updateLogCommand.id}")
        .withHttpHeaders(tempoAuth)
        .put(
          updateLogCommand.updated
        )
      id = response.body[JsValue].as[JsObject].value("tempoWorklogId").as[JsNumber].value.toLongExact
    } yield UpdateLogResponse(id)


}
