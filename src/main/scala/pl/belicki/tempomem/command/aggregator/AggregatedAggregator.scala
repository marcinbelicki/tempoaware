package pl.belicki.tempomem.command.aggregator

import org.jline.reader.Completer
import org.jline.reader.impl.completer.AggregateCompleter

import scala.jdk.CollectionConverters.SeqHasAsJava

trait AggregatedAggregator extends Aggregator {

  def subAggregators: LazyList[Aggregator with UnapplierAggregator]

  private object SubAggregators {
    def unapply(command: String): Option[(Aggregator, String)] =
      subAggregators
        .flatMap(_.unapply(command))
        .headOption
  }

  override val Aggregate: PartialFunction[String, (Aggregator, String)] = {
    case SubAggregators(aggregator, rest) => (aggregator, rest)
  }


  override lazy val commandCompleter: Completer = new AggregateCompleter(
    subAggregators.map(_.commandCompleter).asJava
  )
}
