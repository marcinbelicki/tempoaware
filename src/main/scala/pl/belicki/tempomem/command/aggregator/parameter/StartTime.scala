package pl.belicki.tempomem.command.aggregator.parameter

import cats.data.{Chain, Ior, IorNec, NonEmptyChain}
import pl.belicki.tempomem.info.Info
import pl.belicki.tempomem.info.Info.{InfoType, IorNecChain}

import java.time.LocalTime
import java.time.format.DateTimeParseException

class StartTime[V](
                    protected val transformValue:  IorNecChain[LocalTime] => V
                  ) extends Parameter[IorNecChain[LocalTime], V] {
  protected val short: String = "-st"
  protected val long: String = "--start-time"
  protected val description: String = "Start time of the worklog"

  protected def transformString(value: String): IorNecChain[LocalTime] =
    try Ior.right(Chain(LocalTime.parse(value)))
    catch {
      case parseException: DateTimeParseException => Ior.leftNec(Info(s"Could not read start-time from ${parseException.getParsedString}, message: ${parseException.getMessage}", InfoType.Error))
    }

}
