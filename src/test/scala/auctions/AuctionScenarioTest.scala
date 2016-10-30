package auctions

import actions.{Bid, Notify, StartAuction}
import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestKitBase, TestProbe}
import messages.GetCurrentAuctionValue
import org.scalatest.{FlatSpec, Matchers}
import users.Buyer.RaiseBid

/**
  * Created by neo on 28.10.16.
  */
class AuctionScenarioTest extends FlatSpec with Matchers with TestKitBase with ImplicitSender {

  implicit lazy val system = ActorSystem()
  val auctionSearch = TestProbe()
  val seller = TestProbe()
  val buyer1 = TestProbe()
  val buyer2 = TestProbe()

  val auctionName = "Super BMW"
  val auction = system.actorOf(Auction.props(auctionName, seller.ref))

  "Auction" should "return 0 value when asked for it" in {
    auction ! StartAuction
    buyer1.send(auction, GetCurrentAuctionValue)
    buyer1.expectMsg(0)
  }

  it should "accept a minimum Bid from Buyer1" in {
    buyer1.send(auction, Bid(1))
    buyer2.send(auction, GetCurrentAuctionValue)
    buyer2.expectMsg(1)
  }

  it should "accept a Bid of 3 from Buyer2 and notify Buyer1" in {
    buyer2.send(auction, Bid(3))
    buyer1.expectMsg(RaiseBid(3))
    auction ! GetCurrentAuctionValue
    expectMsg(3)
  }

  it should "not accept a Bid of 2 from Buyer1 and notify Buyer1" in {
    buyer1.send(auction, Bid(2))
    buyer1.expectMsg(RaiseBid(3))
    auction ! GetCurrentAuctionValue
    expectMsg(3)
  }

  it should "send Notify messages to Seller and winner" in {
    buyer2.expectMsg(Notify(3))
    seller.expectMsg(Notify(3))
  }
}
