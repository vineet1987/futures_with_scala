package domain.model

import java.util.concurrent.Executors

import akka.NotUsed
import akka.actor.typed.ActorSystem
import akka.stream.OverflowStrategy
import akka.stream.scaladsl.Source
import domain.service.RbiStub
import domain.model.Transaction.{Deposit, Withdrawal}

import scala.concurrent.{ExecutionContext, Future}

class Account(rbi: RbiStub)(implicit actorSystem: ActorSystem[_]) {

  private var balance                 = 0
  var transactions: List[Transaction] = Nil

  implicit val ec: ExecutionContext = ExecutionContext.fromExecutorService(Executors.newFixedThreadPool(1))

  private val (queue, stream) = Source.queue[Transaction](1024, OverflowStrategy.dropHead).preMaterialize()

  def deposit(amount: Int): Future[Unit] = rbi.notification().map { bool =>
    if (bool) {
      balance += amount
      var deposit : Transaction = Deposit(amount)
      transactions ::= deposit
      queue.offer(deposit)
    }
  }

  def withdraw(amount: Int): Future[Unit] = rbi.notification().map { _ =>
    balance -= amount
    var withdrawal : Transaction = Withdrawal(amount)
    transactions ::= Withdrawal(amount)
    queue.offer(withdrawal)
  }

  def getBalance: Future[Int] = Future {
    balance
  }

  def getStream :Source[Transaction,NotUsed] = stream

}
