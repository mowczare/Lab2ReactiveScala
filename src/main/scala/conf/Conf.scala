package conf

import com.typesafe.config.ConfigFactory

/**
  * Created by neo on 21.10.16.
  */
object Conf {
  lazy val conf = ConfigFactory.load()

  lazy val defaultAuctionSystemName = conf.getString("auctionSystem.name")
  lazy val defaultRemoteAuctionPublisherName = conf.getString("remoteAuctionPublisher.name")
  lazy val defaultAuctionSearchName = conf.getString("auctionSearch.name")
  lazy val defaultMasterSearchName = conf.getString("masterSearch.name")
  lazy val defaultNotifierName = conf.getString("notifier.name")

  lazy val defaultAuctionSearchWorkers = conf.getInt("auctionSearch.workersNumber")

  lazy val defaultAuctionSystemConfig = conf.getConfig("auctionSystemServer").withFallback(conf)
  lazy val defaultRemoteAuctionPublisherServerConfig = conf.getConfig("remoteAuctionPublisherServer")
    .withFallback(conf)

  lazy val defaultAuctionTime = conf.getInt("auction.defaultTime")
  lazy val defaultBidsPerBuyer = conf.getInt("buyer.defaultBids")
  lazy val defaultBidFrequency = conf.getInt("buyer.defaultFrequency")
  lazy val defaultBuyerLaziness = conf.getInt("buyer.defaultLaziness")
}
