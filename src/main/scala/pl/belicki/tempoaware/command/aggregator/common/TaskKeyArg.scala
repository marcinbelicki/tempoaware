package pl.belicki.tempoaware.command.aggregator.common

import cats.data._
import org.jline.reader.Completer
import pl.belicki.tempoaware.command.aggregator.argument.{BareArgument, TaskKey}
import pl.belicki.tempoaware.command.aggregator.{
  Aggregator,
  WithArgumentsAggregator
}
import pl.belicki.tempoaware.info.Info
import pl.belicki.tempoaware.info.Info.{InfoType, IorNecChain, IorTNec}

import scala.concurrent.{ExecutionContext, Future}

trait TaskKeyArg {
  this: Aggregator with WithArgumentsAggregator =>

  protected def addTaskKey(taskKey: IorNecChain[String]): Aggregator

  protected def taskKeyCompleter: Completer

  override protected lazy val arguments: LazyList[BareArgument[_, Aggregator]] =
    LazyList(
      new TaskKey(
        taskKeyCompleter,
        addTaskKey
      )
    )

}

object TaskKeyArg {
  def resolve(
      taskKey: IorNec[Info, Chain[String]]
  )(implicit ec: ExecutionContext): IorTNec[String] =
    IorT
      .fromIor[Future](
        ior = taskKey
          .flatMap {
            case Chain(oneKey) => Ior.right(oneKey)
            case Chain.nil =>
              Ior.leftNec(
                Info("There was no taskKey provided.", InfoType.Error)
              )
            case _ =>
              Ior.leftNec(Info("Too many taskKeys provided.", InfoType.Error))
          }
      )
}
