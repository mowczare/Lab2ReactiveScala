package auctionsearch

import actions.RegisterAuction
import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import auctionsearch.AuctionSearch.AckRegister
import messages.FindAuctions

/**
  * Created by neo on 22.10.16.
  */
class AuctionSearch extends Actor with ActorLogging {

  var auctions: Map[String, ActorRef] = Map()

  override def receive: Receive = {
    case RegisterAuction(name) =>
      auctions = auctions + (name -> sender)
      sender ! AckRegister

    case FindAuctions(regex) =>
      sender ! auctions.keys.filter(s => s contains regex).map(auctions).toList
  }
}

object AuctionSearch {
  def props = Props[AuctionSearch]
  case object AckRegister
}
