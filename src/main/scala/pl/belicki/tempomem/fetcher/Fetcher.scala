package pl.belicki.tempomem.fetcher

import pl.belicki.tempomem.info.Info.iorTNecFromFuture
import pl.belicki.tempomem.info.Info.IorTNec

import scala.concurrent.{ExecutionContext, Future}

trait Fetcher[K, R] {

  def fetchFuture(key: K): Future[R]

  def fetch(key: K)(implicit ec: ExecutionContext): IorTNec[R] = fetchFuture(key)

}
