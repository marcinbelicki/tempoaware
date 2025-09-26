package pl.belicki.tempoaware.command

import pl.belicki.tempoaware.command.response.Response
import pl.belicki.tempoaware.info.Info.IorTNec

import scala.concurrent.ExecutionContext

trait Command {

  def execute(commandConnector: CommandConnector)(implicit
      ec: ExecutionContext
  ): IorTNec[Response]

}
