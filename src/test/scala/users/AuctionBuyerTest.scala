package users

import actions.{Bid, RegisterAuction}
import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestKitBase, TestProbe}
import auctionsearch.AuctionSearch
import conf.Conf
import messages.GetCurrentAuctionValue
import org.scalatest.{FlatSpec, Matchers}
import users.Buyer.RaiseBid

/**
  * Created by neo on 28.10.16.
  */
class AuctionBuyerTest extends FlatSpec with Matchers with TestKitBase with ImplicitSender {

  implicit lazy val system = ActorSystem()

  val as = system.actorOf(AuctionSearch.props, Conf.defaultAuctionSearchName)

  val auction = TestProbe()
  auction.send(as, RegisterAuction("Auto Bahn"))

  val buyer = system.actorOf(Buyer.props("Auto", 10))

  "Buyer" should "do bid of value 1 in action" in {
    auction.expectMsg(GetCurrentAuctionValue)
    auction.reply(0)
    auction.expectMsg(Bid(1))
  }

  it should "reraise to 3 when auction is bid to 2" in {
    auction.send(buyer, RaiseBid(2))
    auction.expectMsg(Bid(3))
  }

  it should "no more reraise when auction is bid to 10 or more" in {
    auction.send(buyer, RaiseBid(10))
    auction.expectNoMsg
    auction.send(buyer, RaiseBid(12))
    auction.expectNoMsg
  }
}
