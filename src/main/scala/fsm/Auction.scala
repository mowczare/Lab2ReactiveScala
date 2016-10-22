package fsm

import akka.actor.{FSM, Props}
import conf.Conf
import actions._
import fsm.Auction._

import scala.concurrent.duration._

/**
  * Created by neo on 21.10.16.
  */
class Auction(name: String) extends FSM[State, Data] {

  startWith(Idle, NoPrice)

  when(Idle) {
    case Event(StartAuction, NoPrice) =>
      context.system.scheduler.scheduleOnce(Conf.defaultAuctionTime milliseconds, self, FinishAuction)
      goto(Created)
  }

  when(Created) {
    case Event(FinishAuction, NoPrice) =>
      context.system.scheduler.scheduleOnce(Conf.defaultAuctionTime milliseconds, self, DeleteItem)
      goto(Ignored)

    case Event(Bid(value), NoPrice) =>
      goto(Activated) using CurrentPrice(value)
  }

  when(Activated) {
    case Event(Bid(value), oldData@CurrentPrice(price)) =>
      if (price<value) {
        log.info(s"Bid $value accepted.")
        stay() using CurrentPrice(value)
      }
      else {
        log.warning(s"Bid $value is smaller than current price of this auction ($price).")
        stay() using oldData
      }

    case Event(FinishAuction, _) =>
      context.system.scheduler.scheduleOnce(Conf.defaultAuctionTime milliseconds, self, DeleteItem)
      //todo notify Buyer
      //todo notify Seller
      goto(Sold)
  }

  when(Sold) {
    case Event(DeleteItem, _) =>
      stop()
  }

  when(Ignored) {
    case Event(DeleteItem, NoPrice) =>
      stop()

    case Event(Relist, NoPrice) =>
      context.system.scheduler.scheduleOnce(Conf.defaultAuctionTime milliseconds, self, FinishAuction)
      goto(Created)
  }

}

object Auction {

  sealed trait State
  case object Idle extends State
  case object Created extends State
  case object Activated extends State
  case object Ignored extends State
  case object Sold extends State

  sealed trait Data
  case object NoPrice extends Data
  case class CurrentPrice(value: Int) extends Data

  def props(name: String) = (Props[Auction], name)
}