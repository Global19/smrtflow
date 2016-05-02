package com.pacbio.secondary.smrtserver.services.jobtypes

import java.util.UUID

import akka.actor.ActorRef
import akka.pattern.ask
import akka.util.Timeout
import com.pacbio.common.dependency.Singleton
import com.pacbio.common.services.PacBioServiceErrors.UnprocessableEntityError
import com.pacbio.secondary.smrtlink.actors.{EngineManagerActorProvider, JobsDaoActorProvider}
import com.pacbio.secondary.smrtlink.services.jobtypes.JobTypeService
import com.pacbio.secondary.smrtlink.services.JobManagerServiceProvider
import com.pacbio.secondary.smrtserver.models.SecondaryAnalysisJsonProtocols
import com.pacbio.secondary.analysis.jobs.CoreJob
import com.pacbio.secondary.analysis.jobs.JobModels.{JobEvent, EngineJob}
import com.pacbio.secondary.analysis.jobtypes.ConvertImportFastaOptions
import com.pacbio.secondary.smrtlink.actors.JobsDaoActor._
import com.pacbio.secondary.smrtlink.models._
import com.typesafe.scalalogging.LazyLogging

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

import spray.json._
import spray.http.MediaTypes
import spray.httpx.SprayJsonSupport
import SprayJsonSupport._


class ImportFastaServiceType(dbActor: ActorRef, engineManagerActor: ActorRef)
  extends JobTypeService with LazyLogging {

  import SecondaryAnalysisJsonProtocols._

  override val endpoint = "convert-fasta-reference"
  override val description = "Import fasta reference and create a generated a Reference DataSet XML file."

  override val routes =
    pathPrefix(endpoint) {
      pathEndOrSingleSlash {
        get {
          complete {
            (dbActor ? GetJobsByJobType(endpoint)).mapTo[Seq[EngineJob]]
          }
        } ~
        post {
          entity(as[ConvertImportFastaOptions]) { sopts =>
            val uuid = UUID.randomUUID()
            val coreJob = CoreJob(uuid, sopts)
            val comment = s"Import/Convert Fasta File to DataSet"

            val fx = Future {sopts.validate}.flatMap {
              case Some(e) => Future { throw new UnprocessableEntityError(s"Failed to validate: $e") }
              case _ => (dbActor ? CreateJobType(
                uuid,
                s"Job $endpoint",
                comment,
                endpoint,
                coreJob,
                None,
                sopts.toJson.toString())).mapTo[EngineJob]
            }

            complete {
              created {
                fx
              }
            }
          }
        }
      } ~
      sharedJobRoutes(dbActor)
    }
}

trait ImportFastaServiceTypeProvider {
  this: JobsDaoActorProvider
      with EngineManagerActorProvider
      with JobManagerServiceProvider =>

  val importFastaServiceType: Singleton[ImportFastaServiceType] =
    Singleton(() => new ImportFastaServiceType(jobsDaoActor(), engineManagerActor())).bindToSet(JobTypes)
}