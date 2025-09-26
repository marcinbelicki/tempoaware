package pl.belicki.tempoaware.command.aggregator.parameter

import cats.data.{Chain, Ior}
import pl.belicki.tempoaware.info.Info.IorNecChain

class Description[V](
    protected val transformValue: IorNecChain[String] => V
) extends Parameter[IorNecChain[String], V] {
  protected val short: String       = "-d"
  protected val long: String        = "--description"
  protected val description: String = "Description of the worklog"

  protected def transformString(value: String): IorNecChain[String] =
    Ior.right(Chain(value))

}
