package pl.belicki.tempoaware.command.aggregator

trait UnapplierAggregator {
  this: Aggregator =>

  def unapply(command: String): Option[(Aggregator, String)]

}
