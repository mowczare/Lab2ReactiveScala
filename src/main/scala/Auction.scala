import akka.actor.{Actor, ActorLogging, PoisonPill}
import conf.Conf
import actions._

import scala.concurrent.duration._

/**
  * Created by neo on 21.10.16.
  */
class Auction(name: String) extends Actor with ActorLogging {

  var currentPrice: Option[Int] = None

  override def receive: Receive = idle

  def idle: Receive = {
    case StartAuction =>
      context.system.scheduler.scheduleOnce(Conf.defaultAuctionTime milliseconds, self, FinishAuction)
      context become created
  }

  def created: Receive = {
    case FinishAuction =>
      context.system.scheduler.scheduleOnce(Conf.defaultAuctionTime milliseconds, self, DeleteItem)
      context become ignored

    case Bid(value) =>
      currentPrice = Some(value)
      context become activated
  }

  def activated: Receive = {
    case Bid(value) =>
      currentPrice.foreach { price =>
        if (price<value) {
          log.info(s"Bid $value accepted.")
          currentPrice = Some(value)
        }
        else {
          log.warning(s"Bid $value is smaller than current price of this auction ($price).")
        }
      }

    case FinishAuction =>
      context.system.scheduler.scheduleOnce(Conf.defaultAuctionTime milliseconds, self, DeleteItem)
      //todo notify Buyer
      //todo notify Seller
      context become sold
  }

  def sold: Receive = {
    case DeleteItem =>
      self ! PoisonPill
  }

  def ignored: Receive = {
    case DeleteItem =>
      self ! PoisonPill

    case Relist =>
      context.system.scheduler.scheduleOnce(Conf.defaultAuctionTime milliseconds, self, FinishAuction)
      context become created
  }

}
