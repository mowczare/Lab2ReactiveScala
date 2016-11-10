package actions

/**
  * Created by neo on 21.10.16.
  */

sealed trait Command

sealed trait AuctionCommand extends Command

case class RegisterAuction(name: String) extends AuctionCommand

case object StartAuction extends AuctionCommand

case object DeleteItem extends AuctionCommand

case object Relist extends AuctionCommand

case class Bid(value: Int) extends AuctionCommand

case object FinishAuction extends AuctionCommand

case class Notify(value: Int) extends AuctionCommand
