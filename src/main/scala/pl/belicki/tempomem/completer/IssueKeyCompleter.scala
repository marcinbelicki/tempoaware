package pl.belicki.tempomem.completer

import org.jline.reader.{Candidate, Completer, LineReader, ParsedLine}
import pl.belicki.tempomem.command.CommandConnector
import pl.belicki.tempomem.info.Info.IorTNec

import java.util
import java.util.concurrent.atomic.AtomicReference
import scala.concurrent.{ExecutionContext, Future}

class IssueKeyCompleter extends Completer {
  private val taskCandidates = new AtomicReference[Seq[Candidate]](List.empty)

  override def complete(reader: LineReader, line: ParsedLine, candidates: util.List[Candidate]): Unit =
    taskCandidates
      .get()
      .foreach(candidates.add)

  def refreshLatestWorklogs(commandConnector: CommandConnector)(implicit executionContext: ExecutionContext): IorTNec[Unit] =
    for {
      latestIssueCandidates <- commandConnector.getLatestWorklogsCandidates
    } yield taskCandidates.set(latestIssueCandidates)

}
