import akka.actor.ActorSystem
import conf.Conf
import remote.AuctionPublisher

/**
  * Created by neo on 01.12.16.
  */
object RemoteMain extends App {

  val remoteSystem = ActorSystem(Conf.defaultRemoteAuctionPublisherName,
     Conf.defaultRemoteAuctionPublisherServerConfig)

   val auctionPublisher = remoteSystem.actorOf(AuctionPublisher.props, Conf.defaultRemoteAuctionPublisherName)
}
