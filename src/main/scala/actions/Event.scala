package actions

import akka.actor.ActorRef

/**
  * Created by neo on 04.11.16.
  */
sealed trait Event

sealed trait AuctionEvent extends Event

case class AuctionRegistered(name: String, timeStamp: Long) extends AuctionEvent

case class AuctionStarted(timeStamp: Long) extends AuctionEvent

case class ItemDeleted(timeStamp: Long) extends AuctionEvent

case class Relisted(timeStamp: Long) extends AuctionEvent

case class BidEvent(value: Int, winner: ActorRef, timeStamp: Long) extends AuctionEvent

case class AuctionSold(timeStamp: Long) extends AuctionEvent

case class AuctionIgnored(timeStamp: Long) extends AuctionEvent

case class Scheduled(timeStamp: Long, command: AuctionCommand) extends AuctionEvent