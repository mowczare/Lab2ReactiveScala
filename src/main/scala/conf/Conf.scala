package conf

import com.typesafe.config.ConfigFactory

/**
  * Created by neo on 21.10.16.
  */
object Conf {
  lazy val conf = ConfigFactory.load()
  lazy val defaultAuctionTime = conf.getInt("auction.defaultTime")
}
