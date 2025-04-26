package pl.belicki.tempomem.command

import pl.belicki.tempomem.command.response.Response
import pl.belicki.tempomem.info.Info.IorTNec

import scala.concurrent.ExecutionContext

trait Command {

  def execute(commandConnector: CommandConnector)(implicit ec: ExecutionContext): IorTNec[Response]

}
