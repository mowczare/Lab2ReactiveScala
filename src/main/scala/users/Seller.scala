package users

import actions.{Notify, StartAuction}
import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import auctions.fsm.Auction
import users.Seller.{GetAuctions, GetNumberOfAuctions}
import utils.StringUtils

/**
  * Created by neo on 22.10.16.
  */
class Seller(auctionNames: List[String]) extends Actor with ActorLogging {

  var auctions: List[ActorRef] = List()

  override def preStart = {
    auctionNames.foreach { name =>
      log.info(s"Put auction $name on list")
      val auction = context.actorOf(Auction.props(name, self), StringUtils.makeActorName(name))
      auctions ::= auction
      auction ! StartAuction
    }
  }

  override def receive: Receive = {
    case Notify(value) =>
      log.info(s"Auction $sender has been finished with value of $value")

    case GetNumberOfAuctions =>
      sender() ! auctions.length

    case GetAuctions =>
      sender() ! auctions
  }
}

object Seller {
  case object GetNumberOfAuctions
  case object GetAuctions
  def props(auctionNames: List[String]): Props = Props(new Seller(auctionNames))
}
