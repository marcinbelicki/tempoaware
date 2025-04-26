package pl.belicki.tempomem.command.aggregator

import cats.data.{IorT, NonEmptyChain}
import org.jline.reader.Completer
import pl.belicki.tempomem.command.aggregator.log.EmptyLogAggregator
import pl.belicki.tempomem.command.{Command, CommandConnector}
import pl.belicki.tempomem.info.Info
import Info.{InfoType, IorTNec}

import scala.concurrent.ExecutionContext

class EmptyAggregator(taskKeyCompleter: Completer, undoAggregator: UndoAggregator) extends Aggregator with AggregatedAggregator {

  override def toCommand(commandConnector: CommandConnector)(implicit ec: ExecutionContext): IorTNec[Command] = {
    IorT.leftT(NonEmptyChain.one(Info("Command not recognized.", InfoType.Error)))
  }

  override lazy val subAggregators: LazyList[Aggregator with UnapplierAggregator] = LazyList(
    new EmptyLogAggregator(taskKeyCompleter),
    undoAggregator,
    ExtendLastAggregator,
    ExitAggregator
  )
}
