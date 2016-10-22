package messages

/**
  * Created by neo on 22.10.16.
  */

trait Message

case object GetCurrentAuctionValue extends Message

case object GetCurrentWinner extends Message

case class FindAuctions(regex: String) extends Message
