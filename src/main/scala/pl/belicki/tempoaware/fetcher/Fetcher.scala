package pl.belicki.tempoaware.fetcher

import pl.belicki.tempoaware.info.Info.iorTNecFromFuture
import pl.belicki.tempoaware.info.Info.IorTNec

import scala.concurrent.{ExecutionContext, Future}

trait Fetcher[K, R] {

  def fetchFuture(key: K): Future[R]

  def fetch(key: K)(implicit ec: ExecutionContext): IorTNec[R] = fetchFuture(
    key
  )

}
