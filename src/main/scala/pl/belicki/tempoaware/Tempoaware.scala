package pl.belicki.tempoaware

import cats.data.Ior
import org.apache.pekko.Done
import org.apache.pekko.actor.ActorSystem
import org.apache.pekko.stream.scaladsl.{Sink, Source}
import org.apache.pekko.stream.{
  ActorAttributes,
  Materializer,
  Supervision,
  SystemMaterializer
}
import org.jline.reader.{LineReader, LineReaderBuilder}
import org.slf4j.LoggerFactory
import pl.belicki.tempoaware.command.CommandConnector
import pl.belicki.tempoaware.command.aggregator.{
  Aggregator,
  EmptyAggregator,
  UndoAggregator
}
import pl.belicki.tempoaware.completer.IssueKeyCompleter
import play.api.libs.ws.ahc._

import scala.concurrent.Future
import scala.util.control.NonFatal

object Tempoaware {
  private val logger = LoggerFactory.getLogger("Tempoaware")

  private val issueKeyCompleter = new IssueKeyCompleter
  private val envMap            = sys.env

  private val tempoToken = envMap("TEMPO_TOKEN")
  private val jiraToken  = envMap("JIRA_TOKEN")
  private val jiraUser   = envMap("JIRA_USER")
  private val jiraUrl    = envMap("JIRA_URL")

  private implicit val system: ActorSystem = ActorSystem()
  private implicit val materializer: Materializer = SystemMaterializer(
    system
  ).materializer
  private implicit val wsClient: StandaloneAhcWSClient = StandaloneAhcWSClient()

  import system.dispatcher

  system.registerOnTermination {
    System.exit(0)
  }

  private val undoAggregator = new UndoAggregator
  private val commandConnector = new CommandConnector(
    tempoToken,
    jiraToken,
    jiraUser,
    jiraUrl,
    undoAggregator
  )
  private val emptyAggregator =
    new EmptyAggregator(issueKeyCompleter, undoAggregator)

  private val reader: LineReader = LineReaderBuilder
    .builder()
    .completer(emptyAggregator.commandCompleter)
    .build()

  private def runStream: Future[Done] = Source
    .repeat(())
    .map(_ => reader.readLine("> "))
    .map(Aggregator.aggregate(emptyAggregator))
    .takeWhile(_.isNotExit)
    .mapAsync(1) { aggregator =>
      val iorT = for {
        command  <- aggregator.toCommand(commandConnector)
        response <- command.execute(commandConnector)
        _        <- issueKeyCompleter.refreshLatestWorklogs(commandConnector)
      } yield response

      iorT.value
        .map {
          case Ior.Both(problems, response) =>
            logger.info(response.message)
            logger.warn(
              s"There were following problems with executing command:\n${problems.toNonEmptyList.toList
                  .mkString("\n")}"
            )
          case Ior.Left(problems) =>
            logger.error(
              s"Command not executed. There were following problems with executing command:\n${problems.toNonEmptyList.toList
                  .mkString("\n")}"
            )
          case Ior.Right(response) =>
            logger.info(response.message)
        }

    }
    .withAttributes(
      ActorAttributes.supervisionStrategy { case NonFatal(throwable) =>
        logger.error(
          s"There was an unexpected problem with your command: ${throwable.getMessage}"
        )
        Supervision.Resume
      }
    )
    .runWith(Sink.ignore)

  def main(args: Array[String]): Unit = {
    logger.info("Starting...")
    for {
      _ <- issueKeyCompleter
        .refreshLatestWorklogs(commandConnector)
        .valueOr(_ => ())
      _ <- runStream
    } yield {
      wsClient.close()
      system.terminate()
    }
  }

}
