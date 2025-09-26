package pl.belicki.tempoaware.command.aggregator

trait ValueExtractor[T, V] {

  protected def extractString(command: String): Option[(String, String)]

  protected def transformString(value: String): T

  protected def transformValue: T => V

  def unapply(command: String): Option[(V, String)] =
    for {
      (value, rest) <- extractString(command)
    } yield (transformValue(transformString(value)), rest)

}
