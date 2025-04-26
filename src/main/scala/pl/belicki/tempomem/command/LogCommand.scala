package pl.belicki.tempomem.command

import cats.data.IorT
import pl.belicki.tempomem.command.response.Response
import pl.belicki.tempomem.info.Info.IorTNec

import java.time.{LocalDate, LocalDateTime, LocalTime}
import scala.concurrent.ExecutionContext

case class LogCommand(
                       issueId: Long,
                       startDateTime: LocalDateTime,
                       timeSpentSeconds: Long,
                       authorAccountId: String,
                       description: String
                     ) extends Command {
  lazy val startDate: LocalDate = startDateTime.toLocalDate
  lazy val startTime: LocalTime = startDateTime.toLocalTime

  override def execute(commandConnector: CommandConnector)(implicit ec: ExecutionContext): IorTNec[Response] = IorT.liftF(commandConnector.log(this))
}
