package pl.belicki.tempoaware.command.aggregator.log

import pl.belicki.tempoaware.command.aggregator.common.TaskKeyArg
import pl.belicki.tempoaware.command.aggregator.parameter._
import pl.belicki.tempoaware.command.aggregator.{
  Aggregator,
  WithArgumentsAggregator
}
import pl.belicki.tempoaware.info.Info.IorNecChain

import java.time.{LocalDate, LocalTime}

trait LogAggregator
    extends Aggregator
    with WithArgumentsAggregator
    with TaskKeyArg {

  override val name: String = "log"

  protected def addStartTime(startTime: IorNecChain[LocalTime]): LogAggregator

  protected def addStartDate(startDate: IorNecChain[LocalDate]): LogAggregator

  protected def addEndTime(endTime: IorNecChain[LocalTime]): LogAggregator

  protected def addEndDate(endDate: IorNecChain[LocalDate]): LogAggregator

  protected def addDescription(description: IorNecChain[String]): LogAggregator

  override protected lazy val parameters: LazyList[Parameter[_, Aggregator]] =
    LazyList(
      new StartTime(addStartTime),
      new StartDate(addStartDate),
      new EndTime(addEndTime),
      new EndDate(addEndDate),
      new Description(addDescription)
    )

}
