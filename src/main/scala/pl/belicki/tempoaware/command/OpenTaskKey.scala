package pl.belicki.tempoaware.command

import pl.belicki.tempoaware.command.DesktopSupport.desktop
import pl.belicki.tempoaware.command.response.{OpenResponse, Response}
import pl.belicki.tempoaware.info.Info.IorTNec

import java.net.URI
import scala.concurrent.ExecutionContext

case class OpenTaskKey(
    taskKey: String
) extends Command {
  override def execute(commandConnector: CommandConnector)(implicit
      ec: ExecutionContext
  ): IorTNec[Response] =
    desktop
      .map(_.browse(new URI(s"${commandConnector.jiraUrl}/browse/$taskKey")))
      .map(_ => OpenResponse)
}
