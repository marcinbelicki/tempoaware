package pl.belicki.tempoaware.command.aggregator

import org.jline.reader.Completer
import org.jline.reader.impl.completer.NullCompleter
import pl.belicki.tempoaware.command.{Command, CommandConnector}
import pl.belicki.tempoaware.info.Info.IorTNec

import scala.annotation.tailrec
import scala.concurrent.ExecutionContext

trait Aggregator {

  val Aggregate: PartialFunction[String, (Aggregator, String)]

  def toCommand(commandConnector: CommandConnector)(implicit
      ec: ExecutionContext
  ): IorTNec[Command]

  def commandCompleter: Completer = NullCompleter.INSTANCE

  def isNotExit: Boolean = true

}

object Aggregator {

  def aggregate(emptyAggregator: Aggregator)(command: String): Aggregator = {
    @tailrec
    def helper(aggregator: Aggregator, rest: String): Aggregator = {
      rest match {
        case aggregator.Aggregate((aggregator, rest)) =>
          helper(aggregator, rest)
        case _ => aggregator
      }
    }

    helper(emptyAggregator, command)
  }
}
