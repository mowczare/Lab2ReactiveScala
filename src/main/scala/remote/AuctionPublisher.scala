package remote

import akka.actor.{Actor, ActorLogging, Props}
import notifier.Notifier.Notify
import remote.AuctionPublisher.AckNotify

/**
  * Created by neo on 30.11.16.
  */

/**
  * To simulate invalid hash, change value inside AckEnvelope response
  * To simulate invalid response, change AckEnvelope response to sth else
  */
class AuctionPublisher extends Actor with ActorLogging {

  override def preStart = {
    super.preStart
    log.info("Started Remote Auction Publisher")
  }

  override def receive: Receive = {
    case e: Notify =>
      log.info("Got Notify")
      sender ! AckNotify(e.hashCode)
  }
}

object AuctionPublisher {
  case class AckNotify(hash: Int)

  def props: Props = Props[AuctionPublisher]
}