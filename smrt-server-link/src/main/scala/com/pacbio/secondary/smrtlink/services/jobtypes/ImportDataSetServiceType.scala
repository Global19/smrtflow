package com.pacbio.secondary.smrtlink.services.jobtypes

import java.util.UUID

import akka.actor.ActorRef
import akka.pattern.ask
import akka.util.Timeout


import scala.concurrent.duration._
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

import spray.http.MediaTypes
import spray.json._
import spray.httpx.SprayJsonSupport
import SprayJsonSupport._


import com.pacbio.common.dependency.Singleton
import com.pacbio.common.services.PacBioServiceErrors.UnprocessableEntityError
import com.pacbio.secondary.analysis.jobs.CoreJob
import com.pacbio.secondary.analysis.jobs.JobModels._
import com.pacbio.secondary.analysis.jobtypes.ImportDataSetOptions
import com.pacbio.secondary.smrtlink.actors.JobsDaoActor._
import com.pacbio.secondary.smrtlink.actors.{EngineManagerActorProvider, JobsDaoActorProvider}
import com.pacbio.secondary.smrtlink.models._
import com.pacbio.secondary.smrtlink.services.JobManagerServiceProvider


class ImportDataSetServiceType(dbActor: ActorRef, engineManagerActor: ActorRef) extends JobTypeService {

  import SmrtLinkJsonProtocols._

  override val endpoint = "import-dataset"
  override val description = "Import a Pacbio DataSet XML file"


  def validate(sopts: ImportDataSetOptions): Future[ImportDataSetOptions] = {
    Future { ValidateImportDataSetUtils.validateDataSetImportOpts(sopts) }.flatMap {
      case Some(err) => Future.failed(new UnprocessableEntityError(s"Failed to validate dataset $err. Options $sopts"))
      case _ => Future { sopts }
    }
  }

  def createJob(sopts:ImportDataSetOptions): Future[EngineJob] = {
    logger.info(s"Attempting to create import-dataset Job with options $sopts")

    val uuid = UUID.randomUUID()
    val desc = s"Importing DataSet"
    val name = s"Job $endpoint"

    val fx = for {
      vopts <- validate(sopts)
      engineJob <- (dbActor ? CreateJobType(uuid, name, desc, endpoint,  CoreJob(uuid, sopts), None, sopts.toJson.toString())).mapTo[EngineJob]
    } yield engineJob

    fx
  }


  override val routes =
    pathPrefix(endpoint) {
      pathEndOrSingleSlash {
        get {
          complete {
            (dbActor ? GetJobsByJobType(endpoint)).mapTo[Seq[EngineJob]]
          }
        } ~
        post {
          entity(as[ImportDataSetOptions]) { sopts =>
                complete {
                  created {
                    createJob(sopts)
                  }
                }
            }
          }
      } ~
      sharedJobRoutes(dbActor)
    }
}

trait ImportDataSetServiceTypeProvider {
  this: JobsDaoActorProvider
      with EngineManagerActorProvider
      with JobManagerServiceProvider =>

  val importDataSetServiceType: Singleton[ImportDataSetServiceType] =
    Singleton(() => new ImportDataSetServiceType(jobsDaoActor(), engineManagerActor())).bindToSet(JobTypes)
}