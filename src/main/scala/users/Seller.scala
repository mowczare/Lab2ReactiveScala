package users

import actions.{Notify, StartAuction}
import akka.actor.{Actor, ActorLogging, Props}
import auctions.fsm.Auction
import utils.StringUtils

/**
  * Created by neo on 22.10.16.
  */
class Seller(auctionNames: List[String]) extends Actor with ActorLogging {

  override def preStart = {
    auctionNames.foreach { name =>
      log.info(s"Put auction $name on list")
      context.actorOf(Auction.props(name, self), StringUtils.makeActorName(name)) ! StartAuction
    }
  }

  override def receive: Receive = {
    case Notify(value) =>
      log.info(s"Auction $sender has been finished with value of $value")
  }
}

object Seller {
  def props(auctionNames: List[String]): Props = Props(new Seller(auctionNames))
}
