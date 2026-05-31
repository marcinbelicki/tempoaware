package pl.belicki.tempoaware.command

import cats.data.{IorT, NonEmptyChain}
import pl.belicki.tempoaware.info.Info
import pl.belicki.tempoaware.info.Info.{InfoType, IorTNec}

import java.awt.Desktop
import scala.concurrent.ExecutionContext

object DesktopSupport {
  private lazy val desktopEither = Either.cond(
    Desktop.isDesktopSupported,
    Desktop.getDesktop,
    NonEmptyChain.one(
      Info("No desktop environment detected.", InfoType.Error)
    )
  )

 def desktop(implicit ec: ExecutionContext): IorTNec[Desktop] =
    IorT.cond(
      Desktop.isDesktopSupported,
      Desktop.getDesktop,
      NonEmptyChain.one(
        Info("No desktop environment detected.", InfoType.Error)
      )
    )
}
