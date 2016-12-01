package notifier

import akka.actor.{Actor, ActorLogging, ActorSelection, PoisonPill, Props}
import akka.pattern.ask
import akka.util.Timeout
import notifier.Notifier.Notify
import notifier.NotifierRequest.{InvalidHashCodeException, InvalidResponseException, RemoteTimeoutException, ThrownException}
import remote.AuctionPublisher.AckNotify

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.language.postfixOps
import scala.util.{Failure, Success}

/**
  * Created by neo on 30.11.16.
  */
class NotifierRequest(auctionPublisher: ActorSelection, e: Notify) extends Actor with ActorLogging {

  implicit val timeout: Timeout = 2 seconds

  override def preStart = {
    super.preStart
    log.info(s"Trying to notify remote server of $e")
      (auctionPublisher ? e).onComplete {
        case Failure(ex) => self ! ThrownException(RemoteTimeoutException(ex.getMessage))
        case Success(AckNotify(hash)) =>
          if (hash != e.hashCode) self ! ThrownException(InvalidHashCodeException(s"Expected: ${e.hashCode}, got: $hash"))
          else {
            log.info(s"Successfully sent $e to remote server")
            self ! PoisonPill
          }
        case Success(response) =>
          self ! ThrownException(InvalidResponseException(s"Expected AckNotify(${e.hashCode}), got $response"))
      }
  }

  override def receive: Receive = {
    case thrown: ThrownException => throw thrown.ex
  }
}

object NotifierRequest {

  /**
    * God forgive me for I have sinned.
    * @param ex: exception that cannot be thrown inside of Future context since it's not handled in Supervisor then ;_;
    */
  case class ThrownException(ex: Exception)

  case class InvalidHashCodeException(message: String) extends Exception

  case class InvalidResponseException(message: String) extends Exception

  case class RemoteTimeoutException(message: String) extends Exception

  def props(auctionPublisher: ActorSelection, e: Notify): Props = Props(new NotifierRequest(auctionPublisher, e))
}
