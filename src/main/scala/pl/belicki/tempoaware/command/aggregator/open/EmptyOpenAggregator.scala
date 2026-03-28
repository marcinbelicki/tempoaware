package pl.belicki.tempoaware.command.aggregator.open

import cats.data.{IorT, NonEmptyChain}
import org.jline.reader.Completer
import pl.belicki.tempoaware.command.{Command, CommandConnector}
import pl.belicki.tempoaware.info.Info
import pl.belicki.tempoaware.info.Info.{InfoType, IorNecChain, IorTNec}

import scala.concurrent.ExecutionContext

class EmptyOpenAggregator(protected val taskKeyCompleter: Completer)
    extends OpenAggregator {

  override def toCommand(commandConnector: CommandConnector)(implicit
      ec: ExecutionContext
  ): IorTNec[Command] =
    IorT.leftT(
      NonEmptyChain.one(
        Info("There was no taskKey provided.", InfoType.Error)
      )
    )

  override protected def addTaskKey(
      taskKey: IorNecChain[String]
  ): OpenAggregatorWithArg = OpenAggregatorWithArg(taskKey)

}
