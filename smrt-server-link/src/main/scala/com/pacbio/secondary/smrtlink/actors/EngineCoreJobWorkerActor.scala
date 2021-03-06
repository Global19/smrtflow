package com.pacbio.secondary.smrtlink.actors

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import com.pacbio.common.models.CommonModelImplicits
import CommonMessages._
import com.pacbio.secondary.smrtlink.analysis.tools.timeUtils
import com.pacbio.secondary.smrtlink.jobtypes.ServiceJobRunner

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Try, Success, Failure}

object EngineCoreJobWorkerActor {
  def props(engineManagerActor: ActorRef,
            serviceRunner: ServiceJobRunner): Props =
    Props(new EngineCoreJobWorkerActor(engineManagerActor, serviceRunner))
}

class EngineCoreJobWorkerActor(engineManagerActor: ActorRef,
                               serviceRunner: ServiceJobRunner)
    extends Actor
    with ActorLogging
    with timeUtils {

  val WORK_TYPE: WorkerType = StandardWorkType
  import CommonModelImplicits._

  override def preStart(): Unit = {
    log.debug(s"Starting engine-worker $self")
  }

  override def postStop(): Unit = {
    log.debug(s"Shutting down worker $self")
  }

  def receive: Receive = {
    case RunEngineJob(engineJob) => {

      sender ! StartingWork

      // All functionality should be encapsulated in the service running layer. We shouldn't even really handle the Failure case of the Try a in here
      // Within this runEngineJob, it should handle all updating of state on failure
      log.info(s"Worker $self attempting to run $engineJob")

      // This blocks and is already wrapped in a Try
      val tx = serviceRunner.run(engineJob)

      log.info(s"Worker $self Results from ServiceRunner $tx")

      val completedWork = CompletedWork(self, WORK_TYPE)

      // We don't care about the result. This is captured and handled in the service runner layer
      val message = tx.map(_ => completedWork).getOrElse(completedWork)
      log.info(s"sending $message to $sender from $self")

      // Make an explicit call to the EngineManagerActor. Using sender ! message doesn't appear to work because of
      // the Future context
      engineManagerActor ! message
    }

    case x => log.debug(s"Unhandled Message to Engine Worker $x")
  }
}

object QuickEngineCoreJobWorkerActor {
  def props(engineManagerActor: ActorRef,
            serviceRunner: ServiceJobRunner): Props =
    Props(new QuickEngineCoreJobWorkerActor(engineManagerActor, serviceRunner))
}

class QuickEngineCoreJobWorkerActor(engineManagerActor: ActorRef,
                                    serviceRunner: ServiceJobRunner)
    extends EngineCoreJobWorkerActor(engineManagerActor, serviceRunner) {
  override val WORK_TYPE = QuickWorkType
}
