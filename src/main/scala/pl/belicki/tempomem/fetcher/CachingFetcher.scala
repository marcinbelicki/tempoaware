package pl.belicki.tempomem.fetcher

import com.google.common.cache.CacheBuilder
import scalacache.{Cache, Entry}
import scalacache.guava.GuavaCache
import scalacache.memoization.memoizeF
import scalacache.modes.scalaFuture.mode

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration.Duration

trait CachingFetcher[K, R] extends Fetcher[K, R] {
  private val underlyingGuavaCache =
    CacheBuilder.newBuilder().maximumSize(10000L).build[String, Entry[R]]
  private implicit val scalaCacheGuava: Cache[R] = GuavaCache(
    underlyingGuavaCache
  )

  implicit protected def ec: ExecutionContext

  protected def ttl: Option[Duration]

  abstract override def fetchFuture(key: K): Future[R] =
    memoizeF[Future, R](ttl)(super.fetchFuture(key))

}
