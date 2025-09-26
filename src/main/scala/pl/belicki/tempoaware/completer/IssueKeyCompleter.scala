package pl.belicki.tempoaware.completer

import org.jline.reader.{Candidate, Completer, LineReader, ParsedLine}
import pl.belicki.tempoaware.command.CommandConnector
import pl.belicki.tempoaware.info.Info.IorTNec

import java.util
import java.util.concurrent.atomic.AtomicReference
import scala.concurrent.ExecutionContext

class IssueKeyCompleter extends Completer {
  private val taskCandidates = new AtomicReference[Seq[Candidate]](List.empty)

  override def complete(
      reader: LineReader,
      line: ParsedLine,
      candidates: util.List[Candidate]
  ): Unit =
    taskCandidates
      .get()
      .foreach(candidates.add)

  def refreshLatestWorklogs(
      commandConnector: CommandConnector
  )(implicit ec: ExecutionContext): IorTNec[Unit] =
    for {
      latestIssueCandidates <- commandConnector.getLatestWorklogsCandidates
    } yield taskCandidates.set(latestIssueCandidates)

}
