package pl.belicki.tempomem.command.aggregator.parameter

import cats.data.{Chain, Ior}
import pl.belicki.tempomem.info.Info
import pl.belicki.tempomem.info.Info.{InfoType, IorNecChain}

import java.time.LocalTime
import java.time.format.DateTimeParseException

class EndTime[V](
    protected val transformValue: IorNecChain[LocalTime] => V
) extends Parameter[IorNecChain[LocalTime], V] {
  protected val short: String       = "-et"
  protected val long: String        = "--end-time"
  protected val description: String = "End time of the worklog"

  protected def transformString(value: String): IorNecChain[LocalTime] =
    try Ior.right(Chain(LocalTime.parse(value)))
    catch {
      case parseException: DateTimeParseException =>
        Ior.leftNec(
          Info(
            s"Could not read end-time from ${parseException.getParsedString}, message: ${parseException.getMessage}",
            InfoType.Error
          )
        )
    }
}
