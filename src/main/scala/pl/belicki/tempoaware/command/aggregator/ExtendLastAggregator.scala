package pl.belicki.tempoaware.command.aggregator

import org.jline.builtins.Completers.{OptDesc, OptionCompleter}
import org.jline.reader.Completer
import org.jline.reader.impl.completer.{ArgumentCompleter, StringsCompleter}
import pl.belicki.tempoaware.command.{Command, CommandConnector}
import pl.belicki.tempoaware.info.Info.IorTNec

import scala.concurrent.ExecutionContext
import scala.jdk.CollectionConverters.SeqHasAsJava

object ExtendLastAggregator
    extends Aggregator
    with UnapplierAggregator
    with OneWordAggregator {

  override protected val word: String = "extend\\s*last"

  override def toCommand(commandConnector: CommandConnector)(implicit
      ec: ExecutionContext
  ): IorTNec[Command] = commandConnector.getUpdateLastToNowCommand

  override lazy val commandCompleter: Completer = {
    val optionCompleter
        : java.util.function.Function[String, java.util.Collection[OptDesc]] =
      _ => Nil.asJava
    new ArgumentCompleter(
      new StringsCompleter("extend"),
      new OptionCompleter(
        new StringsCompleter("last"),
        optionCompleter,
        1
      )
    )
  }
}
