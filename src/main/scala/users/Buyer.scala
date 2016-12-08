package users

import actions.{Bid, Notify}
import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.pattern.ask
import akka.util.Timeout
import conf.Conf
import messages.{FindAuctions, GetCurrentAuctionValue}
import users.Buyer.{BidAuction, RaiseBid, random}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration._
import scala.language.postfixOps
import scala.util.Random

/**
  * Created by neo on 22.10.16.
  */
class Buyer(auctionRegex: String, limit: Int) extends Actor with ActorLogging {

  implicit val timeout: Timeout = 5 seconds
  lazy val masterSearch = context.actorSelection(s"/user/${Conf.defaultMasterSearchName}")
  lazy val auctionsFutures: Future[List[ActorRef]] = (masterSearch ? FindAuctions(auctionRegex)).mapTo[List[ActorRef]]

  override def preStart(): Unit = {
    val latency = random.nextInt(Conf.defaultBuyerLaziness)
    context.system.scheduler.scheduleOnce((Conf.defaultBidFrequency + latency) milliseconds, self, BidAuction)
  }

  override def receive: Receive = {

    case BidAuction =>
      auctionsFutures.map { auctions =>
        auctions.foreach { auction =>
          (auction ? GetCurrentAuctionValue).mapTo[Int].map { value =>
            log.info(s"Bidding auction $auction with value: ${value+1}")
            auction ! Bid(value + 1)
          }
        }
      }

    case RaiseBid(value) =>
      if (value < limit) {
        log.info(s"Last bid raised! Bidding auction $sender with value: ${value+1}")
        Thread.sleep(500)
        sender ! Bid(value + 1)
      }
      else {
        log.info("Cannot into bid. No more money :(")
      }

    case Notify(value) =>
      log.info(s"Won auction $sender with value of $value!")
  }
}

object Buyer {

  trait BuyerAction
  case object BidAuction extends BuyerAction
  case class RaiseBid(value: Int) extends BuyerAction

  lazy val random = new Random

  def props(auctionRegex: String, limit: Int) = Props(new Buyer(auctionRegex, limit))
}
