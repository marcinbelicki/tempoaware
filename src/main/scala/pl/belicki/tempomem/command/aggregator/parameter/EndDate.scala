package pl.belicki.tempomem.command.aggregator.parameter

import cats.data.{Chain, Ior}
import pl.belicki.tempomem.info.Info
import pl.belicki.tempomem.info.Info.{InfoType, IorNecChain}

import java.time.LocalDate
import java.time.format.DateTimeParseException

class EndDate[V](
                  protected val transformValue: IorNecChain[LocalDate] => V
                ) extends Parameter[IorNecChain[LocalDate], V] {
  protected val short: String = "-ed"
  protected val long: String = "--end-date"
  protected val description: String = "Date of the end of the worklog"

  protected def transformString(value: String): IorNecChain[LocalDate] =
    try Ior.right(Chain(LocalDate.parse(value)))
    catch {
      case parseException: DateTimeParseException => Ior.leftNec(Info(s"Could not read start-date from ${parseException.getParsedString}, message: ${parseException.getMessage}", InfoType.Error))
    }

}
