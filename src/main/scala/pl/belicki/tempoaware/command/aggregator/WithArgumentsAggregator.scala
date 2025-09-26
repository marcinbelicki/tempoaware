package pl.belicki.tempoaware.command.aggregator

import org.jline.builtins.Completers.{OptDesc, OptionCompleter}
import org.jline.reader.Completer
import org.jline.reader.impl.completer.{
  ArgumentCompleter,
  NullCompleter,
  StringsCompleter
}
import pl.belicki.tempoaware.command.aggregator.argument.BareArgument
import pl.belicki.tempoaware.command.aggregator.parameter.Parameter

import scala.jdk.CollectionConverters.SeqHasAsJava

trait WithArgumentsAggregator extends UnapplierAggregator {
  this: Aggregator =>

  val name: String

  protected def parameters: LazyList[Parameter[_, Aggregator]]

  protected def arguments: LazyList[BareArgument[_, Aggregator]]

  private lazy val parametersAndArguments
      : LazyList[ValueExtractor[_, Aggregator]] = parameters ++ arguments

  private object AggregatedValue {
    def unapply(command: String): Option[(Aggregator, String)] =
      parametersAndArguments
        .flatMap(_.unapply(command))
        .headOption
  }

  override lazy val commandCompleter: Completer = {
    val optionCompleter
        : java.util.function.Function[String, java.util.Collection[OptDesc]] = {
      case `name` => parameters.map(_.optDesc).asJava
      case _      => Nil.asJava
    }

    new ArgumentCompleter(
      new StringsCompleter(name),
      new OptionCompleter(
        arguments.map(_.completer).appended(NullCompleter.INSTANCE).asJava,
        optionCompleter,
        1
      )
    )
  }

  private lazy val Regex = raw"""(?s)\s*$name\s(.*)""".r

  override val Aggregate: PartialFunction[String, (Aggregator, String)] = {
    case AggregatedValue(aggregator, rest) => (aggregator, rest)
  }

  def unapply(command: String): Option[(Aggregator, String)] =
    Regex
      .unapplySeq(command)
      .collect { case List(rest) =>
        (this, rest)
      }

}
