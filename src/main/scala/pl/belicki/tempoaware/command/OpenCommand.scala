package pl.belicki.tempoaware.command

import cats.data.{IorT, NonEmptyChain}
import pl.belicki.tempoaware.command.OpenCommand.desktop
import pl.belicki.tempoaware.command.response.Response
import pl.belicki.tempoaware.info.Info
import pl.belicki.tempoaware.info.Info.{InfoType, IorTNec}

import java.awt.Desktop
import scala.concurrent.ExecutionContext

case class OpenCommand(
    taskKey: String
) extends Command {
  override def execute(commandConnector: CommandConnector)(implicit
      ec: ExecutionContext
  ): IorTNec[Response] =
    desktop.flatMap(commandConnector.openTaskKey(taskKey, _))

}

object OpenCommand {
  private def desktop(implicit ec: ExecutionContext): IorTNec[Desktop] =
    IorT.cond(
      Desktop.isDesktopSupported,
      Desktop.getDesktop,
      NonEmptyChain.one(
        Info("No desktop environment detected.", InfoType.Error)
      )
    )
}
