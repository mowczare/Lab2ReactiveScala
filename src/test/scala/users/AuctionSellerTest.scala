package users

import akka.actor.{ActorRef, ActorSystem}
import akka.pattern.ask
import akka.testkit.{ImplicitSender, TestKitBase}
import akka.util.Timeout
import org.scalatest.{FlatSpec, Matchers}
import users.Seller.{GetAuctions, GetNumberOfAuctions}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.language.postfixOps


/**
  * Created by neo on 28.10.16.
  */
class AuctionSellerTest extends FlatSpec with Matchers with TestKitBase with ImplicitSender {

  implicit lazy val system = ActorSystem()
  val auctions = List("Auto BMW", "Auto Audi", "ASD")
  val seller = system.actorOf(Seller.props(auctions), "testSeller")
  implicit val timeout: Timeout = 3 seconds

  "Seller" should "create 3 Auction actors" in {
    (seller ? GetAuctions).mapTo[List[ActorRef]].map { auctions =>
      auctions.foreach { auction =>
        auction.path.parent shouldEqual seller
      }
    }
  }

  it should "create 3 Auction actors with proper names" in {
    seller ! GetNumberOfAuctions
    expectMsg(3)
  }

}