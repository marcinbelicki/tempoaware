package pl.belicki.tempoaware.command.aggregator.parameter

import cats.data.{Chain, Ior}
import pl.belicki.tempoaware.info.Info
import pl.belicki.tempoaware.info.Info.{InfoType, IorNecChain}

import java.time.format.DateTimeParseException
import java.time.LocalDate

class StartDate[V](
    protected val transformValue: IorNecChain[LocalDate] => V
) extends Parameter[IorNecChain[LocalDate], V] {
  protected val short: String       = "-sd"
  protected val long: String        = "--start-date"
  protected val description: String = "Start date of the worklog"

  override protected def transformString(
      value: String
  ): IorNecChain[LocalDate] =
    try Ior.right(Chain(LocalDate.parse(value)))
    catch {
      case parseException: DateTimeParseException =>
        Ior.leftNec(
          Info(
            s"Could not read start-date from ${parseException.getParsedString}, message: ${parseException.getMessage}",
            InfoType.Error
          )
        )
    }
}
