package auctions

import actions._
import akka.actor.{Actor, ActorLogging, ActorRef, PoisonPill, Props}
import conf.Conf
import messages.GetCurrentAuctionValue

import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Created by neo on 21.10.16.
  */
class Auction(name: String, seller: ActorRef) extends Actor with ActorLogging {

  lazy val auctionSearch = context.actorSelection(s"/user/${Conf.defaultAuctionSearchName}")
  var currentPrice: Option[Int] = None
  var currentWinner: Option[ActorRef] = None

  override def receive: Receive = idle

  def idle: Receive = {
    case StartAuction =>
      log.info("Starting auction")
      context.system.scheduler.scheduleOnce(Conf.defaultAuctionTime milliseconds, self, FinishAuction)
      auctionSearch ! RegisterAuction(name)
      context become created
  }

  def created: Receive = {
    case FinishAuction =>
      log.info("Finished auction without bid. It is ignored now.")
      context.system.scheduler.scheduleOnce(Conf.defaultAuctionTime milliseconds, self, DeleteItem)
      context become ignored

    case Bid(value) =>
      log.info(s"Bid $value accepted from $sender")
      currentPrice = Some(value)
      currentWinner = Some(sender)
      context become activated

    case GetCurrentAuctionValue =>
      sender ! 0
  }

  def activated: Receive = {
    case Bid(value) =>
      currentPrice.foreach { price =>
        if (price<value) {
          log.info(s"Bid $value accepted from $sender")
          currentPrice = Some(value)
          currentWinner = Some(sender)
        }
        else {
          log.warning(s"Bid $value is smaller than current price of this auction ($price).")
        }
      }

    case FinishAuction =>
      context.system.scheduler.scheduleOnce(Conf.defaultAuctionTime milliseconds, self, DeleteItem)
      for {
        winner <- currentWinner
        price <- currentPrice
      } yield {
        winner ! Notify(price)
        seller ! Notify(price)
        log.info(s"Auction finished. Winner: $winner, price: $price")
      }
      context become sold

    case GetCurrentAuctionValue =>
      currentPrice.foreach(price => sender ! price)
  }

  def sold: Receive = {
    case DeleteItem =>
      log.info("Closing auction")
      self ! PoisonPill
  }

  def ignored: Receive = {
    case DeleteItem =>
      log.info("Closing auction")
      self ! PoisonPill

    case Relist =>
      log.info("Relisting auction")
      context.system.scheduler.scheduleOnce(Conf.defaultAuctionTime milliseconds, self, FinishAuction)
      context become created
  }

}

object Auction {
  def props(name: String, seller: ActorRef): Props = Props(new Auction(name, seller))
}
