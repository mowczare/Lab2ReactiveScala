package auctions.fsm

import akka.actor.{ActorRef, FSM, Props}
import conf.Conf
import actions._
import auctions.fsm.Auction._
import messages.{GetCurrentAuctionValue, GetCurrentWinner}

import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Created by neo on 21.10.16.
  */
class Auction(name: String, seller: ActorRef) extends FSM[State, Data] {

  lazy val auctionSearch = context.actorSelection(s"/user/${Conf.defaultAuctionSearchName}")

  startWith(Idle, NoWinnerData)

  when(Idle) {
    case Event(StartAuction, NoWinnerData) =>
      context.system.scheduler.scheduleOnce(Conf.defaultAuctionTime milliseconds, self, FinishAuction)
      auctionSearch ! RegisterAuction(name)
      goto(Created)
  }

  when(Created) {
    case Event(FinishAuction, NoWinnerData) =>
      context.system.scheduler.scheduleOnce(Conf.defaultAuctionTime milliseconds, self, DeleteItem)
      goto(Ignored)

    case Event(Bid(value), NoWinnerData) =>
      goto(Activated) using CurrentWinnerData(value, sender)
  }

  when(Activated) {
    case Event(Bid(value), oldData@CurrentWinnerData(price, _)) =>
      if (price<value) {
        log.info(s"Bid $value accepted from $sender.")
        stay() using CurrentWinnerData(value, sender)
      }
      else {
        log.warning(s"Bid $value is smaller than current price of this auction ($price).")
        stay() using oldData
      }

    case Event(FinishAuction, CurrentWinnerData(price, winner)) =>
      context.system.scheduler.scheduleOnce(Conf.defaultAuctionTime milliseconds, self, DeleteItem)
      winner ! Notify(price)
      seller ! Notify(price)
      goto(Sold)
  }

  when(Sold) {
    case Event(DeleteItem, _) =>
      log.info("Closing auction")
      stop()
  }

  when(Ignored) {
    case Event(DeleteItem, NoWinnerData) =>
      log.info("Closing auction")
      stop()

    case Event(Relist, NoWinnerData) =>
      context.system.scheduler.scheduleOnce(Conf.defaultAuctionTime milliseconds, self, FinishAuction)
      goto(Created)
  }

  whenUnhandled {
    case Event(GetCurrentAuctionValue, NoWinnerData) =>
      sender ! 0
      stay

    case Event(GetCurrentAuctionValue, CurrentWinnerData(value, _)) =>
      sender ! value
      stay

    case Event(GetCurrentWinner, CurrentWinnerData(_, winner)) =>
      sender ! winner
      stay
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
  case object NoWinnerData extends Data
  case class CurrentWinnerData(value: Int, currentWinner: ActorRef) extends Data

  def props(name: String, seller: ActorRef) = Props(new Auction(name, seller))
}