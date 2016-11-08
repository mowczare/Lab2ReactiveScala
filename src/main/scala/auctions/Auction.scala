package auctions

import actions._
import akka.actor.{ActorLogging, ActorRef, PoisonPill, Props}
import akka.persistence.{PersistentActor, RecoveryCompleted}
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

  var timeStamps: List[ScheduleTimeStamp] = List()

  var currentTimeStamp: Long = System.currentTimeMillis()

  var currentPrice: Option[Int] = None

  var currentWinner: Option[ActorRef] = None

  override def receiveCommand: Receive = idle

  def idle: Receive = {
    case StartAuction =>
      log.info("Starting auction")
      schedule(FinishAuction)
      auctionSearch ! RegisterAuction(name)
      persist(AuctionStarted(timeStamp)) {
        event => context become created
      }
  }

  def created: Receive = {
    case FinishAuction =>
      log.info("Finished auction without bid. It is ignored now.")
      schedule(DeleteItem)
      persist(AuctionIgnored(timeStamp)) {
        event => context become ignored
      }

    case Bid(value) =>
      log.info(s"Bid $value accepted from $sender")
      persist(BidEvent(value, sender, timeStamp)) { event =>
        currentPrice = Some(value)
        currentWinner = Some(sender)
        context become activated
      }

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
          persist(BidEvent(value, sender, timeStamp)) { event =>
            currentPrice = Some(value)
            currentWinner = Some(sender)
          }
        }
        else {
          sender ! RaiseBid(price)
          log.warning(s"Bid $value is smaller than current price of this auction ($price).")
        }
      }

    case FinishAuction =>
      schedule(DeleteItem)
      for {
        winner <- currentWinner
        price <- currentPrice
      } yield {
        winner ! Notify(price)
        seller ! Notify(price)
        log.info(s"Auction finished. Winner: $winner, price: $price")
      }
      persist(AuctionSold, timeStamp) { event =>
        context become sold
      }

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
      schedule(FinishAuction)
      persist(Relisted(timeStamp)) { event =>
        context become created
      }
  }

  override def receiveRecover: Receive = {

    case AuctionStarted(timeStamp) =>
      currentTimeStamp = timeStamp
      context become created

    case AuctionIgnored(timeStamp) =>
      currentTimeStamp = timeStamp
      context become ignored

    case BidEvent(value, winner, timeStamp) =>
      currentTimeStamp = timeStamp
      currentPrice = Some(value)
      currentWinner = Some(winner)
      context become activated

    case AuctionSold(timeStamp) =>
      currentTimeStamp = timeStamp
      context become sold

    case Relisted(timeStamp) =>
      currentTimeStamp = timeStamp
      context become created

    case Scheduled(timeStamp, command) =>
      timeStamps ::= ScheduleTimeStamp(timeStamp, command)

    case RecoveryCompleted =>
      for {
        scheduledTimeStamp <- timeStamps
      } yield {
        if (scheduledTimeStamp.timeStamp + Conf.defaultAuctionTime > currentTimeStamp) {
          context.system.scheduler.scheduleOnce(scheduledTimeStamp.timeStamp + Conf.defaultAuctionTime
            - currentTimeStamp milliseconds, self, scheduledTimeStamp.command)
        }
      }
  }

  private def schedule(command: AuctionCommand): Unit = {
    persist(Scheduled(System.currentTimeMillis(), command)) { event =>
      context.system.scheduler.scheduleOnce(Conf.defaultAuctionTime milliseconds, self, command)
    }
  }

  private def timeStamp = System.currentTimeMillis()

}

object Auction {
  def props(name: String, seller: ActorRef): Props = Props(new Auction(name, seller))
}

case class ScheduleTimeStamp(timeStamp: Long, command: AuctionCommand)
