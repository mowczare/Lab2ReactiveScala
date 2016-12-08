package auctionsearch

import actions.RegisterAuction
import akka.actor.{Actor, ActorLogging, Props}
import akka.routing.{ActorRefRoutee, BroadcastRoutingLogic, RoundRobinRoutingLogic, Router}
import messages.FindAuctions

/**
  * Created by neo on 03.12.16.
  */
class MasterSearch(numberOfRoutees: Int) extends Actor with ActorLogging {

  val routees = Vector.fill(numberOfRoutees) {
    val r = context.actorOf(AuctionSearch.props)
    context watch r
    ActorRefRoutee(r)
  }

  val sellerRouter = Router(BroadcastRoutingLogic(), routees)

  val buyerRouter = Router(RoundRobinRoutingLogic(), routees)


  override def receive: Receive = {
    case msg: RegisterAuction =>
      sellerRouter.route(msg, sender)

    case msg: FindAuctions =>
      buyerRouter.route(msg, sender)
  }
}

object MasterSearch {
  def props(numberOfRoutees: Int) = Props(new MasterSearch(numberOfRoutees))
}
