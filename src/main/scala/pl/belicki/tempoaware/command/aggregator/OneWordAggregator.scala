package pl.belicki.tempoaware.command.aggregator

import org.jline.builtins.Completers.{OptDesc, OptionCompleter}
import org.jline.reader.Completer
import org.jline.reader.impl.completer.{
  ArgumentCompleter,
  NullCompleter,
  StringsCompleter
}

import scala.jdk.CollectionConverters.SeqHasAsJava

trait OneWordAggregator {
  this: Aggregator with UnapplierAggregator =>

  protected def word: String

  private lazy val Regex = raw"""\s*$word\s*""".r

  override def unapply(command: String): Option[(Aggregator, String)] =
    Regex
      .unapplySeq(command)
      .map(_ => (this, ""))

  override val Aggregate: PartialFunction[String, (Aggregator, String)] =
    PartialFunction.empty

  override lazy val commandCompleter: Completer = {
    val optionCompleter
        : java.util.function.Function[String, java.util.Collection[OptDesc]] =
      _ => Nil.asJava

    new ArgumentCompleter(
      new StringsCompleter(word),
      new OptionCompleter(
        NullCompleter.INSTANCE,
        optionCompleter,
        1
      )
    )
  }

}
