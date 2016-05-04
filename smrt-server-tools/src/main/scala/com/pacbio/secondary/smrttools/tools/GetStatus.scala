package com.pacbio.secondary.smrttools.tools

import com.pacbio.secondary.analysis.tools._
import com.pacbio.secondary.smrttools.client.ServiceAccessLayer

import java.net.URL

import akka.actor.ActorSystem
import org.joda.time.DateTime
import scopt.OptionParser
import com.typesafe.scalalogging.LazyLogging

import scala.collection.mutable
import scala.language.postfixOps
import scala.concurrent.duration._
import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
 
import scala.util.Try


case class GetStatusConfig(host: String = "http://localhost",
                           port: Int = 8070,
                           debug: Boolean = false)

/*
 * Get the status of SMRTLink services
 *
 */

trait GetStatusParser {
  final val TOOL_ID = "pbscala.tools.get_status"
  final val VERSION = "0.1.0"
  final val DEFAULT = GetStatusConfig("http://localhost", 8070, debug = false)

  lazy val parser = new OptionParser[GetStatusConfig]("get-status") {
    head("Get SMRTLink status ", VERSION)
    note("Tool to check the status of a currently running smrtlink server")

    opt[String]("host") action { (x, c) =>
      c.copy(host = x)
    } text "Hostname of smrtlink server"

    opt[Int]("port") action { (x, c) =>
      c.copy(port = x)
    } text "Services port on smrtlink server"

    opt[Unit]('h', "help") action { (x, c) =>
      showUsage
      sys.exit(0)
    } text "Show Options and exit"
  }
}

object GetStatusRunner extends LazyLogging {

  def apply (c: GetStatusConfig): Int = {
    val startedAt = DateTime.now()

    implicit val actorSystem = ActorSystem("get-status")
    val url = new URL(s"http://${c.host}:${c.port}")
    logger.debug(s"url: ${url}")
    val sal = new ServiceAccessLayer(url)(actorSystem)
    val fx = for {
      status <- sal.getStatus
    } yield (status)

    val results = Await.result(fx, 5 seconds)
    val (status) = results
    println(status)

    logger.debug("shutting down actor system")
    actorSystem.shutdown()
    0 //exitCode
  }

}

object GetStatusApp extends App with GetStatusParser {
  def run(args: Seq[String]) = {
    val exitCode = parser.parse(args, DEFAULT) match {
      case Some(opts) => GetStatusRunner(opts)
      case _ => 1
    }
    println(s"Exiting $TOOL_ID v$VERSION with exit code $exitCode")
    sys.exit(exitCode)
  }
 
  run(args)
}