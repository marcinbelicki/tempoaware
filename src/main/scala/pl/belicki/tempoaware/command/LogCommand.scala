package pl.belicki.tempoaware.command

import cats.data.IorT
import pl.belicki.tempoaware.command.response.Response
import pl.belicki.tempoaware.info.Info.IorTNec

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

  override def execute(commandConnector: CommandConnector)(implicit
      ec: ExecutionContext
  ): IorTNec[Response] = IorT.liftF(commandConnector.log(this))
}
