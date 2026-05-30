package pl.belicki.tempoaware.command

import org.jline.reader.{Candidate, Completer, LineReader, ParsedLine}
import pl.belicki.tempoaware.command.DesktopSupport.desktop
import pl.belicki.tempoaware.command.response.{OpenResponse, Response}
import pl.belicki.tempoaware.info.Info.IorTNec

import java.net.URI
import java.util
import scala.concurrent.ExecutionContext

case class OpenUri(uri: CommandConnector => URI) extends Command {

  override def execute(commandConnector: CommandConnector)(implicit
      ec: ExecutionContext
  ): IorTNec[Response] =
    desktop.map(_.browse(uri(commandConnector))).map(_ => OpenResponse)
}

object OpenUri {
  private case class Details(name: String, description: String) {
    def matches(key: String): Boolean = key equalsIgnoreCase name
  }
  private val openUriList: List[(Details, Command)] = List(
    Details("jira", "Open your main jira page.")   -> OpenUri(_.jiraUri),
    Details("tempo", "Open your main tempo page.") -> OpenUri(_.tempoUri)
  )

  def unapply(key: String): Option[Command] =
    openUriList.collectFirst {
      case (details, command) if details matches key => command
    }

  lazy val stringsCompleter: Completer = new Completer {
    private val keysList = openUriList.reverse.zipWithIndex.map {
      case ((details, _), index) =>
        new Candidate(
          details.name,
          details.name,
          null,
          details.description,
          null,
          null,
          true,
          Int.MaxValue - index
        )
    }

    override def complete(
        reader: LineReader,
        line: ParsedLine,
        candidates: util.List[Candidate]
    ): Unit =
      keysList.foreach(candidates.add)
  }

}
