package pl.belicki.tempoaware.command.aggregator

import cats.data.{IorT, NonEmptyChain}
import pl.belicki.tempoaware.command.{Command, CommandConnector}
import pl.belicki.tempoaware.info.Info
import pl.belicki.tempoaware.info.Info.{InfoType, IorTNec}

import java.util.concurrent.LinkedBlockingQueue
import scala.concurrent.ExecutionContext

class UndoAggregator
    extends Aggregator
    with UnapplierAggregator
    with OneWordAggregator {

  private val undosQueue = new LinkedBlockingQueue[Command](Int.MaxValue)

  override def toCommand(
      commandConnector: CommandConnector
  )(implicit ec: ExecutionContext): IorTNec[Command] =
    undosQueue.poll() match {
      case null =>
        IorT.leftT(
          NonEmptyChain.one(
            Info("Did not find the undo action.", InfoType.Warn)
          )
        )
      case undo => IorT.pure(undo)
    }

  override protected val word: String = "undo"

  def addUndo(undo: Command): Boolean = undosQueue.add(undo)
}
