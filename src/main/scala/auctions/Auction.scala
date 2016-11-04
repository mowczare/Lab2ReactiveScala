package auctions

import actions._
import akka.actor.{ActorLogging, ActorRef, PoisonPill, Props}
import akka.persistence.PersistentActor
import conf.Conf
import messages.GetCurrentAuctionValue
import users.Buyer.RaiseBid

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.language.postfixOps

/**
  * Created by neo on 21.10.16.
  */
class Auction(name: String, seller: ActorRef) extends PersistentActor with ActorLogging {

  override def persistenceId: String = s"Auction:$name"

  lazy val auctionSearch = context.actorSelection(s"/user/${Conf.defaultAuctionSearchName}")

  var currentPrice: Option[Int] = None

  var currentWinner: Option[ActorRef] = None

  override def receiveCommand: Receive = idle

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
      for {
        price <- currentPrice
        winner <- currentWinner
      } yield {
        if (price < value) {
          log.info(s"Bid $value accepted from $sender")
          winner ! RaiseBid(value)
          currentPrice = Some(value)
          currentWinner = Some(sender)
        }
        else {
          sender ! RaiseBid(price)
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

  override def receiveRecover: Receive = ???

}

object Auction {
  def props(name: String, seller: ActorRef): Props = Props(new Auction(name, seller))
}
