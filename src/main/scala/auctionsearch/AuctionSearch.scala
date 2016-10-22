package auctionsearch

import actions.RegisterAuction
import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import messages.FindAuctions

/**
  * Created by neo on 22.10.16.
  */
class AuctionSearch extends Actor with ActorLogging {

  var auctions: Map[String, ActorRef] = Map()

  override def receive: Receive = {
    case RegisterAuction(name) =>
      auctions = auctions + (name -> sender)

    case FindAuctions(regex) =>
      //todo fix regexp
      val a = auctions.keys.filter(s => s.matches(regex)).map(auctions).toList
      println(a)
      sender ! a
  }
}

object AuctionSearch {
  def props = Props[AuctionSearch]
}