package actions

/**
  * Created by neo on 21.10.16.
  */

sealed trait Action

case object StartAuction extends Action

case object DeleteItem extends Action

case object Relist extends Action

case class Bid(value: Int) extends Action

case object FinishAuction extends Action

case object Notify extends Action
