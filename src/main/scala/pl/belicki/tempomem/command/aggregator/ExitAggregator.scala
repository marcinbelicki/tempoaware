package pl.belicki.tempomem.command.aggregator

import cats.data.{IorT, NonEmptyChain}
import pl.belicki.tempomem.command.{Command, CommandConnector}
import pl.belicki.tempomem.info.Info
import pl.belicki.tempomem.info.Info.{InfoType, IorTNec}

import scala.concurrent.ExecutionContext

object ExitAggregator extends Aggregator with UnapplierAggregator with OneWordAggregator {
  override def isNotExit: Boolean = false

  override protected val word: String = "exit"

  override def toCommand(commandConnector: CommandConnector)(implicit ec: ExecutionContext): IorTNec[Command] =
    IorT.leftT(NonEmptyChain.one(Info("No command for ExitAggregator.", InfoType.Error)))
}
