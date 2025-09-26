package pl.belicki.tempoaware.command.aggregator.argument

import org.jline.reader.Completer
import pl.belicki.tempoaware.command.aggregator.{
  QuotesOrNoSpaceMatcher,
  ValueExtractor
}

import scala.util.matching.Regex

trait BareArgument[T, V] extends ValueExtractor[T, V] {
  protected val Regex: Regex = """(?s) *(\S.*)""".r

  def completer: Completer

  override protected def extractString(
      command: String
  ): Option[(String, String)] =
    Regex
      .unapplySeq(command)
      .collect { case List(QuotesOrNoSpaceMatcher(value, rest)) =>
        (value, rest)
      }
}
