package pl.belicki.tempomem.command.aggregator.argument

import cats.data.{Chain, Ior}
import org.jline.reader.Completer
import pl.belicki.tempomem.info.Info.IorNecChain


class TaskKey[V](
                  val completer: Completer,
                  protected val transformValue: IorNecChain[String] => V
                ) extends BareArgument[IorNecChain[String], V] {
  override protected def transformString(value: String): IorNecChain[String] = Ior.right(Chain(value))
}
