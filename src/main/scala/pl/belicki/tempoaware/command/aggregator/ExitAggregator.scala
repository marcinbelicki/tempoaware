package pl.belicki.tempoaware.command.aggregator

import cats.data.{IorT, NonEmptyChain}
import pl.belicki.tempoaware.command.{Command, CommandConnector}
import pl.belicki.tempoaware.info.Info
import pl.belicki.tempoaware.info.Info.{InfoType, IorTNec}

import scala.concurrent.ExecutionContext

object ExitAggregator
    extends Aggregator
    with UnapplierAggregator
    with OneWordAggregator {
  override def isNotExit: Boolean = false

  override protected val word: String = "exit"

  override def toCommand(
      commandConnector: CommandConnector
  )(implicit ec: ExecutionContext): IorTNec[Command] =
    IorT.leftT(
      NonEmptyChain.one(Info("No command for ExitAggregator.", InfoType.Error))
    )
}
