package pl.belicki.tempoaware.command.aggregator.open

import pl.belicki.tempoaware.command.aggregator.parameter.Parameter
import pl.belicki.tempoaware.command.aggregator.common.TaskKeyArg
import pl.belicki.tempoaware.command.aggregator.{
  Aggregator,
  WithArgumentsAggregator
}

trait OpenAggregator
    extends Aggregator
    with WithArgumentsAggregator
    with TaskKeyArg {

  override val name: String = "open"

  override protected lazy val parameters: LazyList[Parameter[_, Aggregator]] =
    LazyList.empty

}
