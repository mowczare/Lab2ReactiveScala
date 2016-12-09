package auctionsearch

import actions.RegisterAuction
import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, Props}
import akka.pattern.ask
import akka.testkit.{ImplicitSender, TestKitBase}
import akka.util.Timeout
import auctionsearch.AuctionSearch.AckRegister
import auctionsearch.LoadGenerator.{FinishedRegistration, QueryFinished, StartQuery, StartRegistration}
import auctionsearch.LoadManager.LoadCompleted
import conf.Conf
import messages.FindAuctions
import org.scalatest.{FlatSpec, Matchers}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.language.postfixOps
import scala.util.{Failure, Random, Success}

/**
  * Created by neo on 03.12.16.
  */
class MasterSearchPerformanceTest extends FlatSpec with TestKitBase with Matchers with ImplicitSender {
  implicit lazy val system = ActorSystem(Conf.defaultAuctionSystemName, Conf.defaultAuctionSystemConfig)
  val routeesNumber = 7
  val masterSearch = system.actorOf(MasterSearch.props(routeesNumber), Conf.defaultMasterSearchName)
  val loadManager = system.actorOf(LoadManager.props(self, masterSearch, 50000, 10000))

  "AuctionSearch" should "accept multiple RegisterAuction messages and save auctions references" in {
    expectMsg(2 minutes, LoadCompleted)
  }

  /**
    * Query performance results:
    * PC with Intel i7 - 4 cores, 8 threads
    *
    * On RoundRobinRoutingLogic:
    *   - 1 routee - 67 seconds
    *   - 2 routees - 38 seconds
    *   - 3 routees - 19 seconds
    *   - 4 routees - 10.3 seconds
    *   - 5 routees - 11 seconds
    *   - 6 routees - 13 seconds
    *   - 7 routees - 6.2 seconds
    *   - 8 routees - 8.1 seconds
    *   - 16 routees - 11 seconds
    *   - 32 routees - 11 seconds
    *
    *   Best performance on 4, 7 and 8 routees. Measured 5 probes (rest of it just 1):
    *       - 4 routees: 9.5s, 10.7s, 11.2s, 9.8s, 10.4s
    *       - 7 routees: 7.6s, 5.1s, 7.9s, 7,2s, 6.3s
    *       - 8 routees avg: 7.4s, 9.7s, 7.3s, 7.8s, 8.5s
    *
    *
    * On ScatterGatherFirstCompletedRoutingLogic:
    *   - 1 routee - 65 seconds
    *   - 2 routees - 64 seconds
    *   - 4 routees - 40 seconds
    *   - 7 routees - 61 seconds
    *   - 8 routees - 62 seconds
    *   - 16 routees - more than 2 minutes...
    */

}

class LoadGenerator(masterSearch: ActorRef, registrationLoad: Int, queryLoad: Int) extends Actor with ActorLogging {

  implicit val timeout: Timeout = 2 minute

  override def receive: Receive = preRegistration

  def preRegistration: Receive = {
    case StartRegistration =>
      val tempSender = sender()
      (1 to registrationLoad).foreach(s => (masterSearch ? RegisterAuction(LoadGenerator.randomName)).onComplete{
        case Success(AckRegister) => if (s == registrationLoad) {
          context.become(postRegistration)
          tempSender ! FinishedRegistration
        }
      })
  }

  def postRegistration: Receive = {
    case StartQuery =>
      val tempSender = sender()
      (1 to queryLoad).foreach(s => (masterSearch ? FindAuctions(s.toString)).onComplete {
        case Success(auctions) => if (s == queryLoad) tempSender ! QueryFinished
        case Failure(ex) => throw ex
      })
  }
}

object LoadGenerator {
  case object StartRegistration
  case object FinishedRegistration
  case object StartQuery
  case object QueryFinished

  def randomName = Random.nextString(8)
  def props(masterSearch: ActorRef, registrationNumber: Int, queryNumber: Int) = Props(new LoadGenerator(masterSearch, registrationNumber, queryNumber))
}

class LoadManager(master: ActorRef, masterSearch: ActorRef, registrationLoad: Int, queryLoad: Int) extends Actor with ActorLogging {
  val loadGenerator = context.actorOf(LoadGenerator.props(masterSearch, registrationLoad, queryLoad))

  implicit val timeout: Timeout = 2 minute

  override def preStart = {
    super.preStart
    log.info("Starting registration")
    (loadGenerator ? StartRegistration).onComplete {
      case Success(FinishedRegistration) =>
        log.info("Finished registration. Starting querying")
        val startQueryingTimeStamp = System.currentTimeMillis()
        (loadGenerator ? StartQuery).onComplete {
          case Success(QueryFinished) =>
            master ! LoadCompleted
            log.info(s"Querying completed. Total time: ${System.currentTimeMillis()-startQueryingTimeStamp} ms")
          case s => log.error("Query error"+s)
        }
      case _ => log.error("Registration error")
    }
  }

  override def receive: Receive = {
    case _ =>
  }
}

object LoadManager {
  case object LoadCompleted
  def props(master: ActorRef, masterSearch: ActorRef, registrationNumber: Int, queryNumber: Int) = Props(new LoadManager(master, masterSearch, registrationNumber, queryNumber))
}