package users

import actions.{Bid, Notify}
import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.pattern.ask
import akka.util.Timeout
import conf.Conf
import messages.{FindAuctions, GetCurrentAuctionValue}
import users.Buyer.{BidAuction, random}
import scala.concurrent.Future
import scala.util.Random

import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Created by neo on 22.10.16.
  */
class Buyer(auctionRegex: String) extends Actor with ActorLogging {

  implicit val timeout: Timeout = 5 seconds
  lazy val auctionSearch = context.actorSelection(s"/user/${Conf.defaultAuctionSearchName}")
  lazy val auctionsFutures: Future[List[ActorRef]] = (auctionSearch ? FindAuctions(auctionRegex)).mapTo[List[ActorRef]]

  override def preStart(): Unit = {
    (1 to Conf.defaultBidsPerBuyer).foreach { n =>
      val latency = random.nextInt(Conf.defaultBuyerLaziness)
      context.system.scheduler.scheduleOnce((n * Conf.defaultBidFrequency + latency) milliseconds, self, BidAuction)
    }
  }

  override def receive: Receive = {
    case BidAuction =>
      log.info("Trying to bid")
      auctionsFutures.map { auctions =>
        auctions.foreach { auction =>
          if (random.nextBoolean()) {
            (auction ? GetCurrentAuctionValue).mapTo[Int].map { value =>
              log.info(s"Bidding auction $auction")
              auction ! Bid(value + 1)
            }
          }
        }
      }

    case Notify(value) =>
      log.info(s"Won auction $sender with value of $value!")
  }
}

object Buyer {

  trait BuyerAction
  case object BidAuction extends BuyerAction

  lazy val random = new Random

  def props(auctionRegex: String) = Props(new Buyer(auctionRegex))
}
