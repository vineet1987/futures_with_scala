package domain.service

import java.util.concurrent.{Executors, ScheduledExecutorService}

import scala.concurrent.duration.FiniteDuration
import scala.concurrent.{ExecutionContext, Future, Promise, TimeoutException}

class TimerService(implicit ec: ExecutionContext) {

  private val service: ScheduledExecutorService = Executors.newScheduledThreadPool(1)

  def timeout(duration: FiniteDuration): Future[Unit] = {
    val p: Promise[Unit] = Promise()
    service.schedule(() => p.trySuccess(), duration.length, duration.unit)
    p.future
  }

  def completeWithin[T](future: Future[T], timeoutDuration: FiniteDuration): Future[T] = {
    Future.firstCompletedOf(
      List(
        future,
        timeout(timeoutDuration).map(_ => throw new TimeoutException("timed out"))
      )
    )
  }
}
