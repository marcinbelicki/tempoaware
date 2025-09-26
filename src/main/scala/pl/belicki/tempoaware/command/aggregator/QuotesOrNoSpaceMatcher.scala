package pl.belicki.tempoaware.command.aggregator

import scala.annotation.tailrec

object QuotesOrNoSpaceMatcher {
  private val NoSpaceRegex    = """(?s)(\S+)(.*)""".r
  private val StartsWithQuote = """(?s)"(.*)""".r

  private trait QuotesAggregator {
    def currentValue: StringBuilder

    val Aggregate: PartialFunction[Char, QuotesAggregator]
  }

  private object EmptyAggregator extends QuotesAggregator {
    def currentValue: StringBuilder = new StringBuilder()

    override val Aggregate: PartialFunction[Char, QuotesAggregator] = {
      case '"'  => FinishQuotes(currentValue)
      case '\\' => QuoteAsLiteral(currentValue)
      case other =>
        val newCurrentValue = currentValue.addOne(other)
        StandardAggregator(newCurrentValue)
    }
  }

  private case class StandardAggregator(
      currentValue: StringBuilder
  ) extends QuotesAggregator {
    override val Aggregate: PartialFunction[Char, QuotesAggregator] = {
      case '"'  => FinishQuotes(currentValue)
      case '\\' => QuoteAsLiteral(currentValue)
      case other =>
        currentValue.addOne(other)
        this
    }
  }

  private case class QuoteAsLiteral(
      currentValue: StringBuilder
  ) extends QuotesAggregator {
    override val Aggregate: PartialFunction[Char, QuotesAggregator] = {
      case '"' =>
        currentValue.addOne('"')
        StandardAggregator(currentValue)
      case other =>
        currentValue.addOne('\\')
        currentValue.addOne(other)
        StandardAggregator(currentValue)
    }
  }

  private case class FinishQuotes(
      currentValue: StringBuilder
  ) extends QuotesAggregator {
    override val Aggregate: PartialFunction[Char, QuotesAggregator] =
      PartialFunction.empty
  }

  private def aggregate(string: String) = {
    @tailrec
    def helper(aggregator: QuotesAggregator, rest: String): (String, String) = {
      rest.headOption match {
        case Some(aggregator.Aggregate(newAggregator)) =>
          helper(newAggregator, rest.tail)
        case _ => (aggregator.currentValue.result(), rest)
      }
    }

    helper(EmptyAggregator, string)
  }

  def unapply(command: String): Option[(String, String)] = {
    command match {
      case StartsWithQuote(rest)     => Some(aggregate(rest))
      case NoSpaceRegex(value, rest) => Some((value, rest))
      case _                         => None
    }
  }

}
