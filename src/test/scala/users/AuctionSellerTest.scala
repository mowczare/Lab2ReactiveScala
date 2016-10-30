package users

import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestKitBase}
import akka.util.Timeout
import org.scalatest.{FlatSpec, Matchers}
import users.Seller.GetNumberOfAuctions

import scala.concurrent.duration._


/**
  * Created by neo on 28.10.16.
  */
class AuctionSellerTest extends FlatSpec with Matchers with TestKitBase with ImplicitSender {

  implicit lazy val system = ActorSystem()
  val auctions = List("Auto BMW", "Auto Audi", "ASD")
  val seller = system.actorOf(Seller.props(auctions), "testSeller")
  implicit val timeout: Timeout = 3 seconds

  "Seller" should "create 3 Auction actors with proper names" in {
    seller ! GetNumberOfAuctions
    expectMsg(3)
  }

}