package notifier

import akka.actor.SupervisorStrategy.{Escalate, Restart, Stop}
import akka.actor.{Actor, ActorLogging, ActorRef, ActorSelection, OneForOneStrategy, Props}
import conf.Conf
import notifier.Notifier.Notify
import notifier.NotifierRequest.{InvalidHashCodeException, InvalidResponseException, RemoteTimeoutException}

import scala.concurrent.duration._
import scala.language.postfixOps

/**
  * Created by neo on 30.11.16.
  */
class Notifier extends Actor with ActorLogging {

  val auctionPublisher: ActorSelection =
    context.actorSelection(s"akka.tcp://${Conf.defaultRemoteAuctionPublisherName}@127.0.0.1:2553/user/${Conf.defaultRemoteAuctionPublisherName}")

  override val supervisorStrategy =
    OneForOneStrategy(maxNrOfRetries = 10, withinTimeRange = 1 second) {
      case _: RemoteTimeoutException   => log.info("restarting"); Restart
      case _: InvalidHashCodeException => log.info("stopping-hash"); Stop
      case _: InvalidResponseException => log.info("stopping-response"); Stop
      case _: Exception                => log.info("escalating"); Escalate
    }

  override def receive: Receive = {
    case e: Notify =>
      log.info(s"Got $e. Creating Notifier Request Actor.")
      context.actorOf(NotifierRequest.props(auctionPublisher, e))
  }
}

object Notifier {
  case class Notify(title: String, currentWinner: ActorRef, currentPrice: Int)

  def props: Props = Props[Notifier]
}
