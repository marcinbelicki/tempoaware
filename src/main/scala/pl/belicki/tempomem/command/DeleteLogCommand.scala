package pl.belicki.tempomem.command

import cats.data.IorT
import pl.belicki.tempomem.command.response.Response
import pl.belicki.tempomem.info.Info.IorTNec

import scala.concurrent.ExecutionContext

case class DeleteLogCommand(id: Long) extends Command {
  override def execute(commandConnector: CommandConnector)(implicit
      ec: ExecutionContext
  ): IorTNec[Response] =
    IorT.liftF(commandConnector.deleteLog(this))
}
