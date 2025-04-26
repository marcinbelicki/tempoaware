package pl.belicki.tempomem.info

import cats.FlatMap
import cats.data._
import cats.implicits.catsSyntaxSemigroup

import scala.concurrent.{ExecutionContext, Future}
import scala.language.implicitConversions

case class Info(message: String, infoType: Info.InfoType.Value) {
  override def toString: String = s"$infoType: $message"
}

object Info {

  object InfoType extends Enumeration {

    val Info, Warn, Error = Value

  }

  type IorNecChain[V] = IorNec[Info, Chain[V]]

  type IorTNec[V] = IorT[Future, NonEmptyChain[Info], V]

  implicit def iorTNecFromFuture[T](future: Future[T])(implicit ec: ExecutionContext): IorTNec[T] =
    IorT.liftF[Future, NonEmptyChain[Info], T](future)

  implicit def iorTNecFromFutureIor[T](future: Future[Ior[NonEmptyChain[Info], T]]): IorTNec[T] =
    IorT(future)

  implicit def flatMap(implicit ec: ExecutionContext): FlatMap[IorTNec] = new FlatMap[IorTNec] {

    import IorT.catsDataMonadErrorForIorT

    private val innerFlatMap = implicitly[FlatMap[IorTNec]]

    override def flatMap[A, B](fa: IorTNec[A])(f: A => IorTNec[B]): IorTNec[B] = innerFlatMap.flatMap(fa)(f)

    override def tailRecM[A, B](a: A)(f: A => IorTNec[Either[A, B]]): IorTNec[B] = innerFlatMap.tailRecM(a)(f)

    override def map[A, B](fa: IorTNec[A])(f: A => B): IorTNec[B] = innerFlatMap.map(fa)(f)

    override def product[A, B](fa: IorTNec[A], fb: IorTNec[B]): IorTNec[(A, B)] = {
      val value = for {
        valueA <- fa.value
        valueB <- fb.value
      } yield
        valueA match {
          case Ior.Right(a) =>
            valueB match {
              case Ior.Right(b) => Ior.right((a, b))
              case Ior.Both(leftB, rightB) => Ior.both(leftB, (a, rightB))
              case Ior.Left(b) => Ior.left(b)
            }
          case Ior.Both(leftA: NonEmptyChain[Info], rightA) =>
            valueB match {
              case Ior.Right(b) => Ior.both(leftA, (rightA, b))
              case Ior.Both(leftB: NonEmptyChain[Info], rightB) => Ior.both(leftA.combine(leftB), (rightA, rightB))
              case Ior.Left(b) => Ior.left(leftA.combine(b))
            }
          case Ior.Left(a: NonEmptyChain[Info]) =>
            valueB match {
              case Ior.Right(_) => Ior.Left(a)
              case Ior.Both(leftB: NonEmptyChain[Info], _) => Ior.left(a.combine(leftB))
              case Ior.Left(b) => Ior.left(a.combine(b))
            }
        }

      IorT(value)
    }

  }


}
