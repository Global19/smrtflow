package com.pacbio.simulator.steps

import java.nio.file.{Path, Paths}
import java.util.UUID

import com.pacbio.secondary.smrtlink.client.ClientUtils
import com.pacbio.secondary.smrtlink.analysis.datasets.io.{
  DataSetLoader,
  DataSetWriter
}
import com.pacbio.simulator.{
  RunDesignTemplateInfo,
  RunDesignTemplateReader,
  Scenario,
  StepResult
}
import org.apache.commons.io.FileUtils

import scala.concurrent.Future
import resource._

import scala.io.Source
import scala.xml.XML
import XML._

trait IOSteps extends ClientUtils { this: Scenario with VarSteps =>

  import StepResult._

  protected def getUuid(ds: Var[Path]): Var[UUID] =
    ds.mapWith(f => getDataSetMiniMeta(f).uuid)

  case class ReadFileStep(pathVar: Var[String]) extends VarStep[String] {

    override val name = "ReadFile"

    override def runWith =
      Future.successful(
        FileUtils.readFileToString(Paths.get(pathVar.get).toFile))
  }

  case class ReadFileFromTemplate(pathVar: Var[String])
      extends VarStep[String] {

    override val name = "Read File From Template"

    override def runWith = Future {
      var xml = new RunDesignTemplateReader(Paths.get(pathVar.get)).readStr
      //println(s"xml :  $xml")
      xml.mkString
    }
  }

  case class ReadFile(pathVar: Var[String])
      extends VarStep[RunDesignTemplateInfo] {

    override val name = "Read File From Template2"

    override def runWith = Future {
      var runDesignInfo =
        new RunDesignTemplateReader(Paths.get(pathVar.get)).readRundesignTemplateInfo
      // println(s"xml :  ${runDesignInfo.xml}")
      // println(s"subreadSet :  ${runDesignInfo.subreadsetUuid.toString}")
      runDesignInfo
    }
  }

  case class ReadXml(runDesignTemplateInfo: Var[RunDesignTemplateInfo])
      extends VarStep[String] {

    override val name = "Read xml from RunDesignTemplateInfo"

    override def runWith = Future {
      val rr = runDesignTemplateInfo.get
      rr.xml
    }
  }

  case class CheckIfUUIDUpdated(subreads: Var[Path],
                                runInfo: Var[RunDesignTemplateInfo])
      extends Step {
    override val name = "CheckIfUUIDUpdated"

    def checkXml: Result = {
      val uuid = runInfo.get.subreadsetUuid.toString

      val dd = DataSetLoader.loadSubreadSet(subreads.get)
      if (!dd.getUniqueId().equals(runInfo.get.subreadsetUuid.toString))
        FAILED(s"UUID of subreadset xml doesnt match set UUID : ${dd
          .getUniqueId()} != ${runInfo.get.subreadsetUuid.toString}")
      else
        SUCCEEDED
    }

    override def run: Future[Result] = Future {
      checkXml
    }
  }

  case class UpdateSubreadsetXml(subreads: Var[Path],
                                 runInfo: Var[RunDesignTemplateInfo])
      extends Step {
    //with XmlAttributeManipulator{

    override val name = "UpdateSubreadsetXml"

    def updateXml = {
      val uuid = runInfo.get.subreadsetUuid.toString

      val dd = DataSetLoader.loadSubreadSet(subreads.get)
      dd.setUniqueId(uuid)

      //println(s"setting subreadset uuid : $uuid")
      DataSetWriter.writeSubreadSet(dd, subreads.get)
    }

    override def run: Future[Result] = Future {
      updateXml
      SUCCEEDED
    }
  }
}
