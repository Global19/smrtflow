package com.pacbio.secondary.smrtlink.tools

import java.io.{File, IOException}
import java.nio.file.{Files, Path, Paths}
import java.util.UUID

import com.pacbio.common.models.PacBioComponentManifest
import com.pacbio.logging.{LoggerConfig, LoggerOptions}
import com.pacbio.secondary.analysis.configloaders.ConfigLoader
import com.pacbio.secondary.analysis.techsupport.TechSupportUtils
import com.pacbio.secondary.analysis.tools.{CommandLineToolRunner, ToolFailure, ToolSuccess}
import com.pacbio.secondary.smrtlink.models.ConfigModels.RootSmrtflowConfig
import com.pacbio.secondary.smrtlink.models.SmrtLinkJsonProtocols._
import org.apache.commons.io.FileUtils
import scopt.OptionParser
import spray.json._

import scala.util.{Failure, Success, Try}

case class TechSupportFileBundlerOptions(rootUserData: Path, output: Path, user: String,
                                         dnsName: Option[String], smrtLinkVersion: Option[String]) extends LoggerConfig


object TechSupportFileBundler extends CommandLineToolRunner[TechSupportFileBundlerOptions] with ConfigLoader {

  override val VERSION = "0.1.1"
  override val DESCRIPTION = "Create TechSupport bundle for failed SMRT Link Installs"
  override val toolId: String = "smrtflow.tools.tech_support_bundler"

  def getDefault(sx: String): Option[String] = Try { conf.getString(sx)}.toOption

  val defaults = TechSupportFileBundlerOptions(
    Paths.get("userdata"),
    Paths.get("tech-support-bundle.tgz"),
    System.getProperty("user.name"),
    getDefault("smrtflow.server.dnsName"),
    None
  )

  val parser = new OptionParser[TechSupportFileBundlerOptions]("techsupport-bundler") {

    head(DESCRIPTION, VERSION)

    arg[File]("userdata")
        .action { (x, c) => c.copy(rootUserData = x.toPath) }
        .validate(validateRootDir)
        .text(s"Path to Root SMRT Link System userdata dir (e.g, /my-system/root/userdata.")

    opt[String]("output")
        .action { (x, c) => c.copy(output = Paths.get(x).toAbsolutePath) }
        .validate(validateDoesNotExist)
        .text(s"Output TechSupport bundle output (tgz) file. Default '${defaults.output.toAbsolutePath}'")

    opt[String]("user")
        .action { (x, c) => c.copy(user = x) }
        .validate(validateDoesNotExist)
        .text(s"Optional user to create TechSupport bundle output (tgz) file. Default ${defaults.user}")

    // I'm not sure these make sense, but I've added this for testing. These should be configured globally via
    // the smrtlink-system-config.json
    opt[String]("dns")
        .action { (x, c) => c.copy(dnsName = Some(x)) }
        .text("Override for DNS Name of the SL Instance")

    opt[Unit]('h', "help") action { (x, c) =>
      showUsage
      sys.exit(0)
    } text "Show Options and exit"

    opt[Unit]("version") action { (x, c) =>
      showVersion
      sys.exit(0)
    } text "Show tool version and exit"

    LoggerOptions.add(this.asInstanceOf[OptionParser[LoggerConfig]])

    override def errorOnUnknownArgument = false

    override def showUsageOnError = false

  }

  def hasRequired(fx:(Path => Boolean))(path: Path) = {
    if (Files.exists(path: Path) && fx(path)) Success(path)
    else Failure(throw new IOException(s"Unable to find required resource $path"))
  }

  val hasRequiredDir = hasRequired(Files.isDirectory(_))(_)
  val hasRequiredFile = hasRequired(Files.exists(_))(_)

  /**
    * Validate the the config and log subdirs under userdata exist.
    * This is the minimal data that is required.
    *
    * @param rootPath SL Userdata root path
    * @return
    */
  def hasRequiredSubdirs(rootPath: Path): Try[Path] = {

    def resolveTo(sx: String) = rootPath.resolve(sx)

    for {
      _ <- hasRequiredDir(resolveTo(TechSupportUtils.TS_REQ_INSTALL(0)))
      _ <- hasRequiredDir(resolveTo(TechSupportUtils.TS_REQ_INSTALL(1)))
      _ <- hasRequiredFile(resolveTo("config/smrtlink-system-config.json"))
    } yield rootPath
  }

  // Wrap for validation at the Scopt level to fail early
  def validateRootDir(file: File): Either[String, Unit] = {
    hasRequiredSubdirs(file.toPath) match {
      case Success(_) => Right(Unit)
      case Failure(ex) => Left(s"${ex.getMessage}")
    }
  }

  def validateDoesNotExist(sx: String): Either[String, Unit] = {
    val px = Paths.get(sx).toAbsolutePath
    if (Files.exists(px)) Left(s"File already exists. Please move or rename file $px")
    else Right(Unit)
  }

  // This is pretty painful to load the SMRT Link System version and System Id

  private def loadManifest(file: File): Seq[PacBioComponentManifest] = {
    val sx = FileUtils.readFileToString(file, "UTF-8")
    sx.parseJson.convertTo[Seq[PacBioComponentManifest]]
  }

  private def loadSystemConfig(file: File): RootSmrtflowConfig = {
    val sx = FileUtils.readFileToString(file, "UTF-8")
    val c = sx.parseJson.convertTo[RootSmrtflowConfig]
    println(s"Successfully Loaded config from $file")
    c
  }


  private def getSmrtLinkVersionFromConfig(smrtLinkSystemConfig: RootSmrtflowConfig): Option[String] = {
    smrtLinkSystemConfig.smrtflow.server.manifestFile
        .map(p => loadManifest(p.toFile))
        .flatMap(manifests => manifests.find(m => m.id == "smrtlink"))
        .map(n => n.version)
  }

  def getRequired[T](field: String, opt: Option[T]): Try[T] = {
    opt.map(v => Success(v)).getOrElse(Failure(throw new Exception(s"Unable to get required value, $field, from config")))
  }

  override def runTool(c: TechSupportFileBundlerOptions): Try[String] = {
    for {
      systemConfig <- Try {loadSystemConfig(c.rootUserData.resolve(s"config/smrtlink-system-config.json").toFile)}
      systemId <- getRequired[UUID]("SMRT Link System Id", systemConfig.pacBioSystem.smrtLinkSystemId)
      systemVersion <- getRequired[String]("System Version", getSmrtLinkVersionFromConfig(systemConfig))
      tgzPath <-  Try { TechSupportUtils.writeSmrtLinkSystemStatusTgz(systemId, c.rootUserData, c.output, c.user, Some(systemVersion), c.dnsName)}
    } yield s"Successfully wrote TechSupport Bundle to $tgzPath (${tgzPath.toFile.length() / 1024} Kb)"
  }

  // To adhere to the fundamental interface. Other tools need to migrate to use
  // new runnerWithTryAndExit model
  def run(c: TechSupportFileBundlerOptions): Either[ToolFailure, ToolSuccess] =
    Left(ToolFailure(toolId, 0, "NOT Supported"))

}


object TechSupportFileBundlerApp extends App {
  import TechSupportFileBundler._

  runnerWithArgsAndExit(args)
}
