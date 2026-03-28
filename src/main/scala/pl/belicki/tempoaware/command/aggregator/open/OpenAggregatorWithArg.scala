package pl.belicki.tempoaware.command.aggregator.open

import cats.data._
import org.jline.reader.Completer
import org.jline.reader.impl.completer.NullCompleter
import pl.belicki.tempoaware.command.aggregator.common.TaskKeyArg
import pl.belicki.tempoaware.command.{Command, CommandConnector, OpenCommand}
import pl.belicki.tempoaware.info.Info
import pl.belicki.tempoaware.info.Info.{IorNecChain, IorTNec}

import scala.concurrent.ExecutionContext

case class OpenAggregatorWithArg(
    taskKey: IorNec[Info, Chain[String]] = Ior.right(Chain.nil)
) extends OpenAggregator {

  private def finalIssueId(implicit ec: ExecutionContext): IorTNec[String] =
    TaskKeyArg.resolve(taskKey)

  override def toCommand(commandConnector: CommandConnector)(implicit
      ec: ExecutionContext
  ): IorTNec[Command] =
    finalIssueId.map(OpenCommand(_))

  override protected def addTaskKey(
      taskKey: IorNecChain[String]
  ): OpenAggregatorWithArg = copy(taskKey = this.taskKey combine taskKey)

  override protected def taskKeyCompleter: Completer = NullCompleter.INSTANCE
}
