package com.pacbio.secondary.smrtlink.loaders

import java.io._
import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Path}
import javax.xml.bind.{JAXBContext, Marshaller}

import collection.JavaConverters._
import collection.JavaConversions._

import spray.json._
import com.pacificbiosciences.pacbioautomationconstraints.PacBioAutomationConstraints
import com.typesafe.scalalogging.LazyLogging
import org.eclipse.persistence.jaxb.MarshallerProperties

/**
  * Created by mkocher on 2/6/17.
  */
trait PacBioAutomationConstraintsLoader extends LazyLogging {

  private def toUnMarshaller(context: JAXBContext, reader: Reader) = {
    val unmarshaller = context.createUnmarshaller()
    unmarshaller.unmarshal(reader)
  }

  private def contextToMarshaller(jAXBContext: JAXBContext): Marshaller = {
    val jmarshaller = jAXBContext.createMarshaller()
    jmarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true)
    jmarshaller.setProperty(MarshallerProperties.JSON_INCLUDE_ROOT, false)
    jmarshaller.setProperty(MarshallerProperties.MEDIA_TYPE, "application/json")
    jmarshaller
  }

  /**
    * Convert to spray JSON instance
    * @param pacBioAutomationConstraints PacBioAutomation Constraints data model.
    *                                    Contains part numbers and Constraints
    * @return
    */
  def pacBioAutomationToJson(pacBioAutomationConstraints: PacBioAutomationConstraints): JsValue = {
    val jAXBContext = JAXBContext.newInstance(classOf[PacBioAutomationConstraints])
    val outStream = new ByteArrayOutputStream()
    contextToMarshaller(jAXBContext).marshal(pacBioAutomationConstraints, outStream)
    outStream.toString.parseJson
  }

  private def loadFromReader(reader: Reader): PacBioAutomationConstraints =
    toUnMarshaller(JAXBContext.newInstance(classOf[PacBioAutomationConstraints]), reader).asInstanceOf[PacBioAutomationConstraints]

  /**
    * Load the PacBioAutoConstraints from a Path to the XML file
    *
    * @param path
    * @return
    */
  def loadFrom(path: Path): PacBioAutomationConstraints = {
    val reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)
    loadFromReader(reader)
  }

  /**
    * Load a PacBioAutoConstraints from an string of the XML
    *
    * @param sx
    * @return
    */
  def loadFromString(sx: String): PacBioAutomationConstraints =
    loadFromReader(new StringReader(sx))

  private val EXAMPLE_PB_AUTO_XML = "example-chemistry-bundle/definitions/PacBioAutomationConstraints.xml"

  /**
    * Load Example PacBioAutoConstraints from sbt or within the Jar file.
    *
    * This should only be used for testing or development.
    *
    * @return
    */
  def loadExample(): PacBioAutomationConstraints = {
    // This does work
    // The leading '/' is required for sbt, but not for loading from assembly
    println(s"Loading resource $EXAMPLE_PB_AUTO_XML")
    val sx = getClass.getClassLoader.getResourceAsStream(EXAMPLE_PB_AUTO_XML)
    println(s"Loading from $sx")
    val reader = new BufferedReader(new InputStreamReader(sx, "UTF-8"))
    loadFromReader(reader)
  }

}

object PacBioAutomationConstraintsLoader extends PacBioAutomationConstraintsLoader