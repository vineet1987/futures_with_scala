package domain.service

import scala.concurrent.duration.DurationDouble
import scala.concurrent.{ExecutionContext, Future}

class RbiStub(timerService: TimerService)(implicit ec: ExecutionContext) {

  def notification(): Future[Boolean] = {
    timerService.timeout(1.seconds).map(_ => true)
  }

}
