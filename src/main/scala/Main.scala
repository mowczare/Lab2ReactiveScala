import akka.actor.ActorSystem
import auctionsearch.AuctionSearch
import conf.Conf
import users.{Buyer, Seller}

/**
  * Created by neo on 22.10.16.
  */
object Main extends App {
  val system = ActorSystem("auctionSystem")

  val auctionSearch = system.actorOf(AuctionSearch.props, Conf.defaultAuctionSearchName)
  println(auctionSearch.path)

  val seller1 = system.actorOf(Seller.props(List("Auto Czarne Audi", "Auto Czerwona Beemka", "Auto Zielony Fiat")), "seller1")
  val seller2 = system.actorOf(Seller.props(List("PC Lenovo", "PC Asus")), "seller2")

  val buyer1a = system.actorOf(Buyer.props("Auto"), "buyer1a")
  val buyer1b = system.actorOf(Buyer.props("Auto"), "buyer1b")
  val buyer1c = system.actorOf(Buyer.props("Audi"), "buyer1c")
  val buyer2a = system.actorOf(Buyer.props("PC"), "buyer2a")
  val buyer2b = system.actorOf(Buyer.props("Asus"), "buyer2b")
}
