package pl.belicki.tempoaware.command

import cats.data.IorT
import pl.belicki.tempoaware.command.response.Response
import pl.belicki.tempoaware.info.Info.IorTNec

import scala.concurrent.ExecutionContext

case class DeleteLogCommand(id: Long) extends Command {
  override def execute(commandConnector: CommandConnector)(implicit
      ec: ExecutionContext
  ): IorTNec[Response] =
    IorT.liftF(commandConnector.deleteLog(this))
}
