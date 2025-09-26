package pl.belicki.tempomem.command.aggregator.log

import cats.data.{Chain, IorNec, IorT, NonEmptyChain}
import org.jline.reader.Completer
import pl.belicki.tempomem.command.{Command, CommandConnector}
import pl.belicki.tempomem.info.Info
import pl.belicki.tempomem.info.Info.{InfoType, IorNecChain, IorTNec}

import java.time.{LocalDate, LocalTime}
import scala.concurrent.{ExecutionContext, Future}

class EmptyLogAggregator(protected val taskKeyCompleter: Completer)
    extends LogAggregator {

  override protected def addStartTime(
      startTime: IorNecChain[LocalTime]
  ): LogAggregator = LogAggregatorWithParams(startTime = startTime)

  override protected def addStartDate(
      startDate: IorNecChain[LocalDate]
  ): LogAggregator = LogAggregatorWithParams(startDate = startDate)

  override protected def addEndTime(
      endTime: IorNecChain[LocalTime]
  ): LogAggregator = LogAggregatorWithParams(endTime = endTime)

  override protected def addEndDate(
      endDate: IorNecChain[LocalDate]
  ): LogAggregator = LogAggregatorWithParams(endDate = endDate)

  override protected def addDescription(
      description: IorNecChain[String]
  ): LogAggregator = LogAggregatorWithParams(description = description)

  override protected def addTaskKey(
      taskKey: IorNecChain[String]
  ): LogAggregator = LogAggregatorWithParams(taskKey = taskKey)

  override def toCommand(
      commandConnector: CommandConnector
  )(implicit ec: ExecutionContext): IorTNec[Command] =
    IorT.leftT(
      NonEmptyChain.one(
        Info("Too little arguments for creating log.", InfoType.Error)
      )
    )

}
