package actions

/**
  * Created by neo on 04.11.16.
  */
sealed trait Event

sealed trait AuctionEvent extends Event

case class AuctionRegistered(name: String) extends AuctionEvent

case object AuctionStarted extends AuctionEvent

case object ItemDeleted extends AuctionEvent

case object Relisted extends AuctionEvent

case object BidEvent extends AuctionEvent

case object AuctionFinished extends AuctionEvent