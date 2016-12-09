import akka.actor.ActorSystem
import auctionsearch.MasterSearch
import conf.Conf
import notifier.Notifier
import remote.AuctionPublisher
import users.{Buyer, Seller}

/**
  * Created by neo on 22.10.16.
  */
object Main extends App {
  val system = ActorSystem(Conf.defaultAuctionSystemName, Conf.defaultAuctionSystemConfig)
  val masterSearch = system.actorOf(MasterSearch.props(Conf.defaultAuctionSearchWorkers), Conf.defaultMasterSearchName)
  val notifier = system.actorOf(Notifier.props, Conf.defaultNotifierName)

  val seller1 = system.actorOf(Seller.props(List("Auto Czarne Audi 911s")), "seller1")
  val seller2 = system.actorOf(Seller.props(List("PC Lenovo 911s")), "seller2")

  val buyer1a = system.actorOf(Buyer.props("Auto", 10), "buyer1a")
  val buyer2a = system.actorOf(Buyer.props("Auto", 10), "buyer1b")


  /**
    * To simulate remote server unavailability comment lines below and run RemoteMain
    */

   val remoteSystem = ActorSystem(Conf.defaultRemoteAuctionPublisherName,
     Conf.defaultRemoteAuctionPublisherServerConfig)

   val auctionPublisher = remoteSystem.actorOf(AuctionPublisher.props, Conf.defaultRemoteAuctionPublisherName)
}
