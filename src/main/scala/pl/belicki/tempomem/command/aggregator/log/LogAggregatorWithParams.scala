package pl.belicki.tempomem.command.aggregator.log

import cats.data._
import org.jline.reader.Completer
import org.jline.reader.impl.completer.NullCompleter
import pl.belicki.tempomem.command.{Command, CommandConnector, LogCommand}
import pl.belicki.tempomem.fetcher.AccountIdFetcher
import pl.belicki.tempomem.info.Info
import pl.belicki.tempomem.info.Info.{InfoType, IorNecChain, IorTNec}

import java.time._
import scala.concurrent.{ExecutionContext, Future}
import Info.flatMap
import cats.implicits.{catsSyntaxTuple2Semigroupal, catsSyntaxTuple5Semigroupal}

import java.time.temporal.ChronoUnit

case class LogAggregatorWithParams(
    startTime: IorNec[Info, Chain[LocalTime]] = Ior.right(Chain.nil),
    startDate: IorNec[Info, Chain[LocalDate]] = Ior.right(Chain.nil),
    endTime: IorNec[Info, Chain[LocalTime]] = Ior.right(Chain.nil),
    endDate: IorNec[Info, Chain[LocalDate]] = Ior.right(Chain.nil),
    taskKey: IorNec[Info, Chain[String]] = Ior.right(Chain.nil),
    description: IorNec[Info, Chain[String]] = Ior.right(Chain.nil)
) extends LogAggregator {

  private def finalStartTime =
    startTime.flatMap {
      case Chain(oneTime) =>
        if (startTime.isBoth)
          Ior.leftNec(
            Info(
              s"There is one proper start time: $oneTime, but there were problems with the other ones so the start-time could not be obtained.",
              InfoType.Error
            )
          )
        else Ior.right(Some(oneTime))
      case Chain.nil =>
        if (startTime.isBoth)
          Ior.leftNec(Info("There are no proper start times.", InfoType.Error))
        else Ior.right(None)
      case _ =>
        Ior.leftNec(
          Info("There are multiple proper start times.", InfoType.Error)
        )
    }

  private def finalStartDate =
    startDate.flatMap {
      case Chain(oneDate) =>
        if (startDate.isBoth)
          Ior.leftNec(
            Info(
              s"There is one proper start date: $oneDate, but there were problems with the other ones so the start-time could not be obtained.",
              InfoType.Error
            )
          )
        else Ior.right(Some(oneDate))
      case Chain.nil =>
        if (startDate.isBoth)
          Ior.leftNec(Info("There are no proper start dates.", InfoType.Error))
        else Ior.right(None)
      case _ =>
        Ior.leftNec(
          Info("There are multiple proper start dates.", InfoType.Error)
        )
    }

  private def finalEndTime =
    endTime.flatMap {
      case Chain(oneTime) =>
        if (endTime.isBoth)
          Ior.leftNec(
            Info(
              s"There is one proper end time: $oneTime, but there were problems with the other ones so the start-time could not be obtained.",
              InfoType.Error
            )
          )
        else Ior.right(Some(oneTime))
      case Chain.nil =>
        if (endTime.isBoth)
          Ior.leftNec(Info("There are no proper end times.", InfoType.Error))
        else Ior.right(None)
      case _ =>
        Ior.leftNec(
          Info("There are multiple proper end times.", InfoType.Error)
        )
    }

  private def finalEndDate =
    endDate.flatMap {
      case Chain(oneDate) =>
        if (endDate.isBoth)
          Ior.leftNec(
            Info(
              s"There is one proper end date: $oneDate, but there were problems with the other ones so the start-time could not be obtained.",
              InfoType.Error
            )
          )
        else Ior.right(Some(oneDate))
      case Chain.nil =>
        if (endDate.isBoth)
          Ior.leftNec(Info("There are no proper end dates.", InfoType.Error))
        else Ior.right(None)
      case _ =>
        Ior.leftNec(
          Info("There are multiple proper end dates.", InfoType.Error)
        )
    }

  private def startDateTime(commandConnector: CommandConnector)(implicit
      ec: ExecutionContext
  ): IorTNec[Either[LocalTime, LocalDateTime]] = {
    IorT
      .fromIor[Future](
        ior = (finalStartDate, finalStartTime)
          .flatMapN {
            case (Some(oneDate), Some(oneTime)) =>
              Ior.right(Some(Right(LocalDateTime.of(oneDate, oneTime))))
            case (None, Some(oneTime)) => Ior.right(Some(Left(oneTime)))
            case (Some(oneDate), None) =>
              Ior.leftNec(
                Info(
                  s"There was only a start-date: $oneDate provided, and no start-time it's impossible to create reasonable startDateTime.",
                  InfoType.Error
                )
              )
            case _ => Ior.right(None)
          }
      )
      .flatMap {
        case Some(oneDateTime) => IorT.pure(oneDateTime)
        case _ => commandConnector.getLatestWorklogEndDateTime.map(Right(_))
      }

  }

  private def endDateTime(implicit
      ec: ExecutionContext
  ): IorTNec[Either[LocalTime, LocalDateTime]] = {
    IorT.fromIor[Future](
      ior = (finalEndDate, finalEndTime).flatMapN {
        case (Some(oneDate), Some(oneTime)) =>
          Ior.right(Right(LocalDateTime.of(oneDate, oneTime)))
        case (None, Some(oneTime)) => Ior.right(Left(oneTime))
        case (Some(oneDate), None) =>
          Ior.leftNec(
            Info(
              s"There was only a end-date: $oneDate provided, and no end-time it's impossible to create reasonable startDateTime.",
              InfoType.Error
            )
          )
        case _ => Ior.right(Right(LocalDateTime.now))
      }
    )
  }

  private def finalIssueId(
      commandConnector: CommandConnector
  )(implicit ec: ExecutionContext): IorTNec[Long] =
    IorT
      .fromIor[Future](
        ior = taskKey
          .flatMap {
            case Chain(oneKey) => Ior.right(oneKey)
            case Chain.nil =>
              Ior.leftNec(
                Info("There was no taskKey provided.", InfoType.Error)
              )
            case _ =>
              Ior.leftNec(Info("Too many taskKeys provided.", InfoType.Error))
          }
      )
      .map(commandConnector.issueIdFetcher.fetchFuture)
      .flatMap(IorT.liftF[Future, NonEmptyChain[Info], Long](_))

  private def finalDescription(implicit ec: ExecutionContext): IorTNec[String] =
    IorT.fromIor[Future](
      ior = description
        .flatMap {
          case Chain(oneDescription) => Ior.right(oneDescription)
          case Chain.nil =>
            Ior.bothNec(
              Info(
                "There was no description provided, setting up empty string",
                InfoType.Warn
              ),
              ""
            )
          case _ =>
            Ior.leftNec(Info("Too many descriptions provided.", InfoType.Error))
        }
    )

  override def toCommand(
      commandConnector: CommandConnector
  )(implicit ec: ExecutionContext): IorTNec[Command] =
    (
      finalDescription,
      finalIssueId(commandConnector),
      startDateTime(commandConnector),
      endDateTime,
      IorT.liftF(commandConnector.accountIdFetcher.fetchFuture(())): IorTNec[
        AccountIdFetcher.Info
      ]
    ).flatMapN {
      case (
            description,
            issueId,
            eitherStartDateTime,
            eitherEndDateTime,
            AccountIdFetcher.Info(accountId, zoneId)
          ) =>
        val dateNow = LocalDate.now(zoneId)

        val startDateTime = eitherStartDateTime
          .fold(dateNow.atTime, identity)
          .truncatedTo(ChronoUnit.MINUTES)

        val zonedEndDateTime = eitherEndDateTime
          .fold(dateNow.atTime, identity)
          .truncatedTo(ChronoUnit.MINUTES)
          .atZone(zoneId)
        val zonedStartDateTime = ZonedDateTime.of(startDateTime, zoneId)

        val duration = Duration.between(zonedStartDateTime, zonedEndDateTime)
        lazy val timeSpentSeconds = duration.toSeconds

        if (duration.isPositive)
          IorT.rightT(
            LogCommand(
              issueId = issueId,
              startDateTime = startDateTime,
              timeSpentSeconds = timeSpentSeconds,
              authorAccountId = accountId,
              description = description
            )
          )
        else
          IorT.leftT(
            NonEmptyChain.of(
              Info(
                "Start date time is equal or after end date time.",
                InfoType.Error
              )
            )
          )
    }

  override protected def addStartTime(
      startTime: IorNecChain[LocalTime]
  ): LogAggregator = copy(startTime = this.startTime combine startTime)

  override protected def addStartDate(
      startDate: IorNecChain[LocalDate]
  ): LogAggregator = copy(startDate = this.startDate combine startDate)

  override protected def addEndTime(
      endTime: IorNecChain[LocalTime]
  ): LogAggregator = copy(endTime = this.endTime combine endTime)

  override protected def addEndDate(
      endDate: IorNecChain[LocalDate]
  ): LogAggregator = copy(endDate = this.endDate combine endDate)

  override protected def addDescription(
      description: IorNecChain[String]
  ): LogAggregator = copy(description = this.description combine description)

  override protected def addTaskKey(
      taskKey: IorNecChain[String]
  ): LogAggregator = copy(taskKey = this.taskKey combine taskKey)

  override protected def taskKeyCompleter: Completer = NullCompleter.INSTANCE
}
