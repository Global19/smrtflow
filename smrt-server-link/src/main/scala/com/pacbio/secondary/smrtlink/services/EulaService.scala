package com.pacbio.secondary.smrtlink.services

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import akka.actor.ActorRef
import akka.pattern._
import akka.util.Timeout
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import spray.json._
import akka.http.scaladsl.server._
import DefaultJsonProtocol._
import akka.http.scaladsl.model.StatusCodes
import com.pacbio.secondary.smrtlink.models.PacBioComponentManifest
import com.pacbio.secondary.smrtlink.dependency.Singleton
import com.pacbio.secondary.smrtlink.services.PacBioServiceErrors.ResourceNotFoundError
import com.pacbio.secondary.smrtlink.actors.CommonMessages._
import com.pacbio.secondary.smrtlink.JobServiceConstants
import com.pacbio.secondary.smrtlink.actors._
import com.pacbio.secondary.smrtlink.models._
import com.pacbio.common.utils.OSUtils
import com.pacbio.secondary.smrtlink.app.SmrtLinkConfigProvider
import org.joda.time.{DateTime => JodaDateTime}

import scala.concurrent.Future

class EulaService(smrtLinkSystemVersion: Option[String], dao: JobsDao)
    extends BaseSmrtService
    with JobServiceConstants
    with OSUtils {

  import com.pacbio.secondary.smrtlink.jsonprotocols.SmrtLinkJsonProtocols._

  val manifest = PacBioComponentManifest(toServiceId("eula"),
                                         "EULA Service",
                                         "0.1.0",
                                         "End-User License Agreement Service")

  implicit val timeout = Timeout(30.seconds)

  lazy val osVersion = getOsVersion()

  def toEulaRecord(user: String,
                   enableInstallMetrics: Boolean,
                   enableJobMetrics: Boolean,
                   systemVersion: String): EulaRecord = {
    EulaRecord(user,
               JodaDateTime.now(),
               systemVersion,
               osVersion,
               enableInstallMetrics,
               enableJobMetrics = enableJobMetrics)
  }

  def convertToEulaRecord(user: String,
                          enableInstallMetrics: Boolean,
                          enableJobMetrics: Boolean): Future[EulaRecord] = {
    smrtLinkSystemVersion match {
      case Some(version) =>
        Future.successful(
          toEulaRecord(user, enableInstallMetrics, enableJobMetrics, version))
      case _ =>
        Future.failed(ResourceNotFoundError(
          "System was not configured with SMRT Link System version. Unable to accept Eula"))
    }
  }

  // This is crufty. The error handling is captured in the get by version call
  def deleteEula(version: String): Future[SuccessMessage] = {
    dao
      .removeEula(version)
      .map(
        x =>
          if (x == 0)
            SuccessMessage(s"No user agreement for version $version was found")
          else SuccessMessage(s"Removed user agreement for version $version"))
  }

  override val routes =
    pathPrefix("eula") {
      path(Segment) { version =>
        get {
          complete {
            dao.getEulaByVersion(version)
          }
        } ~
          delete {
            complete {
              for {
                _ <- dao.getEulaByVersion(version)
                msg <- deleteEula(version)
              } yield msg
            }
          }
      } ~
        pathEndOrSingleSlash {
          post {
            entity(as[EulaAcceptance]) { sopts =>
              complete(StatusCodes.Created -> {
                for {
                  eulaRecord <- convertToEulaRecord(
                    sopts.user,
                    sopts.enableInstallMetrics,
                    sopts.enableJobMetrics.getOrElse(false))
                  acceptedRecord <- dao.addEulaRecord(eulaRecord)
                } yield acceptedRecord
              })
            }
          } ~
            get {
              complete {
                dao.getEulas
              }
            }
        }
    }

}

trait EulaServiceProvider {
  this: JobsDaoProvider with SmrtLinkConfigProvider with ServiceComposer =>

  val eulaService: Singleton[EulaService] =
    Singleton { () =>
      new EulaService(smrtLinkVersion(), jobsDao())
    }

  addService(eulaService)
}
