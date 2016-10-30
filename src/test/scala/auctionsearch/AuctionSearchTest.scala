package auctionsearch

import actions.RegisterAuction
import akka.actor.{ActorRef, ActorSystem}
import akka.testkit.TestKitBase
import org.scalatest.{FlatSpec, Matchers}
import akka.pattern.ask
import akka.util.Timeout
import messages.FindAuctions

import scala.concurrent.duration._

import scala.concurrent.ExecutionContext.Implicits.global


/**
  * Created by neo on 28.10.16.
  */
class AuctionSearchTest extends FlatSpec with TestKitBase with Matchers {

  implicit lazy val system = ActorSystem()
  val auctionSearch = system.actorOf(AuctionSearch.props)
  implicit val timeout: Timeout = 3 seconds

  "AuctionSearch" should "accept multiple RegisterAuction messages and save auctions references" in {
    auctionSearch ! RegisterAuction("BMW Nowe 123")
    auctionSearch ! RegisterAuction("Audi Stare 123")
    auctionSearch ! RegisterAuction("BMW Stare 321")
    auctionSearch ! RegisterAuction("**!@# 98!@#")
  }

  it should "return correct auctions on FindAuction command" in {
    (auctionSearch ? FindAuctions("BMW")).mapTo[List[ActorRef]].map (refs =>
      refs.length shouldBe 2)
    (auctionSearch ? FindAuctions("Audi")).mapTo[List[ActorRef]].map (refs =>
      refs.length shouldBe 1)
    (auctionSearch ? FindAuctions("Stare")).mapTo[List[ActorRef]].map (refs =>
      refs.length shouldBe 2)
    (auctionSearch ? FindAuctions("123")).mapTo[List[ActorRef]].map (refs =>
      refs.length shouldBe 2)
    (auctionSearch ? FindAuctions("456")).mapTo[List[ActorRef]].map (refs =>
      refs.length shouldBe 0)
  }

}
