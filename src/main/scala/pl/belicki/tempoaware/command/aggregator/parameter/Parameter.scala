package pl.belicki.tempoaware.command.aggregator.parameter

import org.jline.builtins.Completers.OptDesc
import pl.belicki.tempoaware.command.aggregator.{
  QuotesOrNoSpaceMatcher,
  ValueExtractor
}

trait Parameter[T, V] extends ValueExtractor[T, V] {

  protected def short: String

  protected def long: String

  protected def description: String

  private lazy val Regex = raw"""(?s)\s*(?:$short|$long)=(.*)""".r

  lazy val optDesc = new OptDesc(short, long, description)

  override protected def extractString(
      command: String
  ): Option[(String, String)] =
    Regex
      .unapplySeq(command)
      .collect { case List(QuotesOrNoSpaceMatcher(value, rest)) =>
        (value, rest)
      }

}
