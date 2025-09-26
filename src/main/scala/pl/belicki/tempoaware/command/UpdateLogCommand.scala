package pl.belicki.tempoaware.command

import pl.belicki.tempoaware.command.response.Response
import pl.belicki.tempoaware.info.Info.IorTNec
import play.api.libs.json.JsObject

import scala.concurrent.ExecutionContext

case class UpdateLogCommand(
    id: Long,
    updated: JsObject
) extends Command {
  override def execute(commandConnector: CommandConnector)(implicit
      ec: ExecutionContext
  ): IorTNec[Response] = commandConnector.updateLog(this)
}
