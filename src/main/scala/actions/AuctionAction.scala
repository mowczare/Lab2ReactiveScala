package actions

/**
  * Created by neo on 21.10.16.
  */

trait Action

sealed trait AuctionAction extends Action

case class RegisterAuction(name: String) extends AuctionAction

case object StartAuction extends AuctionAction

case object DeleteItem extends AuctionAction

case object Relist extends AuctionAction

case class Bid(value: Int) extends AuctionAction

case object FinishAuction extends AuctionAction

case class Notify(value: Int) extends AuctionAction
