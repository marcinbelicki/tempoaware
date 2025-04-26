package pl.belicki.tempomem.command.aggregator.log

import org.jline.reader.Completer
import pl.belicki.tempomem.command.aggregator.argument.{BareArgument, TaskKey}
import pl.belicki.tempomem.command.aggregator.parameter.{Description, EndDate, EndTime, Parameter, StartDate, StartTime}
import pl.belicki.tempomem.command.aggregator.{Aggregator, WithArgumentsAggregator}
import pl.belicki.tempomem.info.Info.IorNecChain

import java.time.{LocalDate, LocalTime}

trait LogAggregator extends Aggregator with WithArgumentsAggregator {

  override val name: String = "log"

  protected def addStartTime(startTime: IorNecChain[LocalTime]): LogAggregator

  protected def addStartDate(startDate: IorNecChain[LocalDate]): LogAggregator

  protected def addEndTime(endTime: IorNecChain[LocalTime]): LogAggregator

  protected def addEndDate(endDate: IorNecChain[LocalDate]): LogAggregator

  protected def addDescription(description: IorNecChain[String]): LogAggregator

  protected def addTaskKey(taskKey: IorNecChain[String]): LogAggregator

  protected def taskKeyCompleter: Completer

  override protected lazy val parameters: LazyList[Parameter[_, Aggregator]] = LazyList(
    new StartTime(addStartTime),
    new StartDate(addStartDate),
    new EndTime(addEndTime),
    new EndDate(addEndDate),
    new Description(addDescription)
  )

  override protected lazy val arguments: LazyList[BareArgument[_, Aggregator]] = LazyList(
    new TaskKey(
      taskKeyCompleter,
      addTaskKey
    )
  )

}
