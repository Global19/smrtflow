package com.pacbio.secondary.smrtlink.tools

import java.io.File
import java.net.URL
import java.nio.file.{Files, Path, StandardCopyOption}

import scala.util.Try

import com.typesafe.scalalogging.LazyLogging
import org.apache.commons.io.FileUtils
import scopt.OptionParser
import spray.json._
import DefaultJsonProtocol._

import com.pacbio.common.logging.{LoggerConfig, LoggerOptions}
import com.pacbio.secondary.smrtlink.analysis.tools.{
  CommandLineToolRunner,
  ToolFailure
}
import com.pacbio.secondary.smrtlink.client.Wso2Models
import com.pacbio.secondary.smrtlink.models.ConfigModels.{
  RootSmrtflowConfig,
  Wso2Credentials
}
import com.pacbio.secondary.smrtlink.io.XmlTemplateReader
import com.pacbio.secondary.smrtlink.jsonprotocols.ConfigModelsJsonProtocol

case class ApplyConfigToolOptions(rootDir: Path,
                                  templateDir: Option[Path] = None)
    extends LoggerConfig

object ApplyConfigConstants {
  val TOMCAT_VERSION = "tomcat_current"
  val WSO2_VERSION = "wso2am-2.0.0"

  // This must be relative as
  val SL_CONFIG_JSON = "smrtlink-system-config.json"
  val WSO2_CREDENTIALS_JSON = "wso2-credentials.json"
  // See the comments in updateApplicationJson for why this extra config layer exists
  val SL_INTERNAL_CONFIG_JSON = "internal-config.json"
  val SL_APP_CONFIG_JSON = "application.json"

  val JVM_ARGS = "services-jvm-args"
  val JVM_LOG_ARGS = "services-log-args"

  val STATIC_FILE_DIR = "sl"

  val UI_API_SERVER_CONFIG_JSON = "api-server.config.json"
  val UI_APP_CONFIG_JSON = "app-config.json"

  // Tomcat
  val TOMCAT_SERVER_XML = "server.xml"
  val TOMCAT_HTTPS_SERVER_XML = "https-server.xml"
  val TOMCAT_INDEX = "index.jsp"

  val SSL_KEYSTORE_FILE = ".keystore"
  val UI_PROXY_FILE = "_sl-ui_.xml"
  val READONLY_USERSTORE_FILE = "ReadOnlyUserStoreSynapseConfig.xml"
  val WSO2_DB = "wso2am"

  // Relative Tomcat
  val REL_TOMCAT_ENV_SH = s"$TOMCAT_VERSION/bin/setenv.sh"
  val REL_TOMCAT_SERVER_XML = s"$TOMCAT_VERSION/conf/$TOMCAT_SERVER_XML"
  val REL_TOMCAT_REDIRECT_JSP = s"$TOMCAT_VERSION/webapps/ROOT/$TOMCAT_INDEX"

  val REL_TOMCAT_UI_API_SERVER_CONFIG =
    s"$TOMCAT_VERSION/webapps/ROOT/$STATIC_FILE_DIR/$UI_API_SERVER_CONFIG_JSON"

  val REL_TOMCAT_UI_APP_CONFIG =
    s"$TOMCAT_VERSION/webapps/ROOT/$STATIC_FILE_DIR/$UI_APP_CONFIG_JSON"

  val REL_WSO2_API_DIR =
    s"$WSO2_VERSION/repository/deployment/server/synapse-configs/default/api"
  val REL_WSO2_SEQ_DIR =
    s"$WSO2_VERSION/repository/deployment/server/synapse-configs/default/sequences"
  val REL_WSO2_LOG_DIR = s"$WSO2_VERSION/repository/logs"
  val REL_WSO2_CONF_DIR = s"$WSO2_VERSION/repository/conf"

  // Templates
  val T_WSO2_TEMPLATES = "templates-wso2"
  // Tomcat
  val T_TOMCAT_USERS_XML = "tomcat-users.xml"
  val T_TOMCAT_SERVER_XML = "server.xml"
  val T_INDEX_JSP = "index.jsp"

  // Wso2 Related Templates
  val USER_MGT_XML = "user-mgt.xml"
  val JNDI_PROPERTIES = "jndi.properties"
  val METRICS_XML = "metrics.xml"
  val MASTER_DATASOURCES_XML = "master-datasources.xml"
  val MASTER_DATASOURCES_DEST = "datasources/master-datasources.xml"
}

trait Resolver {
  val rootDir: Path
  def resolve(fileName: String) = rootDir.resolve(fileName)
}

/**
  * Resolve files relative the root Bundle Directory
  *
  * @param rootDir
  */
class BundleOutputResolver(override val rootDir: Path) extends Resolver {
  val jvmArgs = resolve(ApplyConfigConstants.JVM_ARGS)

  val jvmLogArgs = resolve(ApplyConfigConstants.JVM_LOG_ARGS)

  val smrtLinkSystemConfig = resolve(ApplyConfigConstants.SL_CONFIG_JSON)

  val smrtLinkAppConfig = resolve(ApplyConfigConstants.SL_APP_CONFIG_JSON)

  val smrtLinkInternalConfig = resolve(
    ApplyConfigConstants.SL_INTERNAL_CONFIG_JSON)

  val tomcatEnvSh = resolve(ApplyConfigConstants.REL_TOMCAT_ENV_SH)

  val tomcatIndexJsp = resolve(ApplyConfigConstants.REL_TOMCAT_REDIRECT_JSP)

  val tomcatServerXml = resolve(ApplyConfigConstants.REL_TOMCAT_SERVER_XML)

  val uiApiServerConfig = resolve(
    ApplyConfigConstants.REL_TOMCAT_UI_API_SERVER_CONFIG)

  val uiAppConfig = resolve(ApplyConfigConstants.REL_TOMCAT_UI_APP_CONFIG)

  val keyStoreFile = resolve(ApplyConfigConstants.SSL_KEYSTORE_FILE)

  val wso2ApiDir = resolve(ApplyConfigConstants.REL_WSO2_API_DIR)

  val wso2SeqDir = resolve(ApplyConfigConstants.REL_WSO2_SEQ_DIR)

  val wso2ConfDir = resolve(ApplyConfigConstants.REL_WSO2_CONF_DIR)

  val uiProxyConfig = wso2ApiDir.resolve(ApplyConfigConstants.UI_PROXY_FILE)

  val userMgtConfig = wso2ConfDir.resolve(ApplyConfigConstants.USER_MGT_XML)

  val metricsXmlConfig = wso2ConfDir.resolve(ApplyConfigConstants.METRICS_XML)

  val masterDatasourcesConfig =
    wso2ConfDir.resolve(ApplyConfigConstants.MASTER_DATASOURCES_DEST)

  val jndiPropertiesConfig =
    wso2ConfDir.resolve(ApplyConfigConstants.JNDI_PROPERTIES)
}

class TemplateOutputResolver(override val rootDir: Path) extends Resolver {

  val wso2TemplateDir = resolve(ApplyConfigConstants.T_WSO2_TEMPLATES)

  private def resolveWso2Template(fileName: String) =
    wso2TemplateDir.resolve(fileName)

  val indexJsp = resolve(ApplyConfigConstants.T_INDEX_JSP)
  val serverXml = resolve(ApplyConfigConstants.T_TOMCAT_SERVER_XML)
  val tomcatUsers = resolve(ApplyConfigConstants.T_TOMCAT_USERS_XML)

  val uiProxyXml = resolveWso2Template(ApplyConfigConstants.UI_PROXY_FILE)
  val readOnlyUserStore = resolveWso2Template(
    ApplyConfigConstants.READONLY_USERSTORE_FILE)
  val userMgtXml = resolveWso2Template(ApplyConfigConstants.USER_MGT_XML)
  val metricsXml = resolveWso2Template(ApplyConfigConstants.METRICS_XML)
  val jndiProperties = resolveWso2Template(
    ApplyConfigConstants.JNDI_PROPERTIES)
  val masterDatasources = resolveWso2Template(
    ApplyConfigConstants.MASTER_DATASOURCES_XML)
}

object ApplyConfigUtils extends LazyLogging {

  import ConfigModelsJsonProtocol._

  private def writeAndLog(file: File, sx: String): File = {
    FileUtils.write(file, sx, "UTF-8")
    logger.debug(s"Wrote file $file")
    file
  }

  def loadJson(file: File) = {
    logger.debug(s"Loading and converting to JSON $file")
    FileUtils.readFileToString(file).parseJson
  }

  def loadSmrtLinkSystemConfig(path: Path): RootSmrtflowConfig =
    FileUtils
      .readFileToString(path.toFile, "UTF-8")
      .parseJson
      .convertTo[RootSmrtflowConfig]

  def loadWso2Credentials(path: Path): Wso2Credentials =
    FileUtils
      .readFileToString(path.toFile, "UTF-8")
      .parseJson
      .convertTo[Wso2Credentials]

  def validateConfig(c: RootSmrtflowConfig): RootSmrtflowConfig = {
    logger.warn(s"validation of config not implemented $c")
    c
  }

  def writeJvmArgs(outputFile: File, minMemory: Int, maxMemory: Int): File = {
    val sx = s"-Xmx${maxMemory}m -Xms${minMemory}m"
    writeAndLog(outputFile, sx)
  }

  def writeJvmLogArgs(outputFile: File, logDir: Path, logLevel: String): File = {
    val sx =
      s"--log-file $logDir/secondary-smrt-server.log --log-level $logLevel"
    writeAndLog(outputFile, sx)
  }

  def setupWso2LogDir(rootDir: Path, logDir: Path): Path = {
    val wso2LogDir = rootDir.resolve(ApplyConfigConstants.REL_WSO2_LOG_DIR)
    val symlink = logDir.resolve("wso2")
    if (!Files.exists(symlink)) {
      Files.createSymbolicLink(symlink, wso2LogDir)
    } else {
      logger.warn(s"Path $symlink already exists")
      symlink
    }
  }

  type M = Map[String, Option[String]]
  case class ApiServerConfig(auth: M,
                             `events-url`: M,
                             `smrt-link`: M,
                             `smrt-link-backend`: M,
                             `smrt-view`: M,
                             `tech-support`: M)

  private def toApiUIServer(authUrl: URL,
                            eventsUrl: Option[URL],
                            smrtLink: URL,
                            smrtLinkBackendUrl: URL,
                            smrtView: URL,
                            techSupport: Option[URL]): ApiServerConfig = {
    def toM(u: URL): M = Map("default-server" -> Some(u.toString))
    def toO(u: Option[URL]): M = Map("default-server" -> u.map(_.toString))
    ApiServerConfig(toM(authUrl),
                    toO(eventsUrl),
                    toM(smrtLink),
                    toM(smrtLinkBackendUrl),
                    toM(smrtView),
                    toO(techSupport))
  }
  private def writeApiServerConfig(c: ApiServerConfig, output: File): File = {
    implicit val converterFormat = jsonFormat6(ApiServerConfig)
    val jx = c.toJson
    writeAndLog(output, jx.prettyPrint)
    output
  }

  /**
    * #6 (update_server_path_in_ui)
    * Update the UI api-server.config.json in Tomcat at /webapps/ROOT/${STATIC_DIR}/api-server.config.json
    * @param outputFile
    */
  def updateApiServerConfig(outputFile: File,
                            authUrl: URL,
                            eventsUrl: Option[URL],
                            smrtLinkUrl: URL,
                            smrtLinkBackendUrl: URL,
                            smrtViewUrl: URL,
                            techSupportUrl: Option[URL] = None): File = {

    val c = toApiUIServer(authUrl,
                          eventsUrl,
                          smrtLinkUrl,
                          smrtLinkBackendUrl,
                          smrtViewUrl,
                          techSupportUrl)
    writeApiServerConfig(c, outputFile)
    outputFile
  }

  /**
    * Use a pass through layer to overwrite the "enableCellReuse" key in the
    * UI app-config.json
    *
    */
  def updateUiAppConfig(uiAppConfigJson: File,
                        enableCellReuse: Boolean): File = {

    val jx = loadJson(uiAppConfigJson)

    val nx = JsObject(
      "enableCellReuse" -> JsBoolean(enableCellReuse),
      "consumerKey" -> JsString(Wso2Models.defaultClient.clientId),
      "consumerSecret" -> JsString(Wso2Models.defaultClient.clientSecret)
    )

    val total = new JsObject(jx.asJsObject.fields ++ nx.fields)

    writeAndLog(uiAppConfigJson, total.toJson.prettyPrint.toString)
  }

  def updateTomcatSetupEnvSh(output: File, tomcatMem: Int): File = {
    // double $ escapes
    val sx = "export CATALINA_OPTS=" + "\"" + s"$$CATALINA_OPTS -Xmx${tomcatMem}m -Xms${tomcatMem}m" + "\"\n"
    writeAndLog(output, sx)
  }

  /**
    * # 7 (update_tomcat)
    * Update Tomcat server.xml Template with Tomcat Port.
    * (REMOVE THIS) Update .keystore file in the root bundle dir (?) is this even used ???
    *
    * @param outputTomcatServerXml
    * @param tomcatPort
    */
  def updateTomcatConfig(outputTomcatServerXml: File,
                         tomcatServerTemplate: File,
                         tomcatPort: Int): File = {

    val xmlNode = XmlTemplateReader
      .fromFile(tomcatServerTemplate)
      .globally()
      .substitute("${TOMCAT_PORT}", tomcatPort)
      .result()

    writeAndLog(outputTomcatServerXml, xmlNode.mkString)
  }

  /**
    * #8 (update_sl_ui)
    * - Write _sl-ui.xml to the root wso2 API dir with the tomcat host, port and static dir (e.g, "sl")
    *
    * @param outputFile
    */
  def updateUIProxyXml(outputFile: File,
                       smrtLinkStaticUITemplateXml: File,
                       host: String,
                       tomcatPort: Int,
                       smrtLinkStaticDir: String): File = {

    val subs: Map[String, () => String] =
      Map("${STATIC_FILE_DIR}" -> (() => smrtLinkStaticDir),
          "${TOMCAT_PORT}" -> (() => tomcatPort.toString),
          "${TOMCAT_HOST}" -> (() => host))

    val xmlNode = XmlTemplateReader
      .fromFile(smrtLinkStaticUITemplateXml)
      .globally()
      .substituteMap(subs)
      .result()

    writeAndLog(outputFile, xmlNode.mkString)
  }

  /**
    * #9 (update_redirect)
    * Write the index.jsp with the wso2 HOST, port and the static dir (e.g., "sl")
    *
    * @return
    */
  def updateWso2Redirect(outputFile: File,
                         indexJspTemplate: File,
                         host: String,
                         wso2Port: Int,
                         staticDirName: String): File = {

    // This is not XML, use a template search/replace model. Note, Host doesn't NOT have the protocol
    val sx = FileUtils.readFileToString(indexJspTemplate)
    val output = sx
      .replace("${STATIC_FILE_DIR}", staticDirName)
      .replace("${WSO2_HOST}", host)
      .replace("${WSO2_PORT}", wso2Port.toString)

    writeAndLog(outputFile, output)
  }

  /**
    * #10, #11 (update_user_mgt, update_jndi_properties)
    * - Sub WSO2 username and password in conf files
    */
  def updateWso2ConfFile(outputFile: File,
                         inputTemplateFile: File,
                         wso2User: String,
                         wso2Password: String): File = {
    val out = FileUtils
      .readFileToString(inputTemplateFile, "UTF-8")
      .replaceAllLiterally("${WSO2_USER}", wso2User)
      .replaceAllLiterally("${WSO2_PASSWORD}", wso2Password)

    writeAndLog(outputFile, out)
  }

  /**
    * #12
    * update synapse config to enforce only reads on the read-only userstore API
    */
  def updateReadOnlyUserstore(wso2SeqDir: Path,
                              inputTemplateFile: File,
                              wso2User: String) = {
    val apiName = "ReadOnlyRemoteUserStoreService"
    val apiVersion = "1"
    val seqName = s"${wso2User}--${apiName}:v${apiVersion}--In"
    val outFile = wso2SeqDir.resolve(s"${seqName}.xml").toFile
    val xmlNode = XmlTemplateReader
      .fromFile(inputTemplateFile)
      .globally()
      .substitute("${SEQUENCE_NAME}", seqName)
      .result()

    writeAndLog(outFile, xmlNode.mkString)
  }

  /**
    * #13
    * disable metrics
    */
  def updateMetricsConfig(outputPath: Path, metricsXml: Path) = {
    Files.copy(metricsXml, outputPath, StandardCopyOption.REPLACE_EXISTING)
  }

  /**
    * #14
    * Configure wso2 for postgres
    */
  def updateMasterDatasources(outputFile: File,
                              masterDatasourceTemplate: File,
                              dbHost: String,
                              dbPort: Int,
                              dbUser: String,
                              dbPass: String) = {
    val subs: Map[String, () => Any] =
      Map(
        "${DB_HOST}" -> (() => dbHost),
        "${DB_PORT}" -> (() => dbPort.toString),
        "${DB_USER}" -> (() => dbUser),
        "${DB_PASS}" -> (() => dbPass),
        "${WSO2_DB}" -> (() => ApplyConfigConstants.WSO2_DB)
      )

    val xmlNode = XmlTemplateReader
      .fromFile(masterDatasourceTemplate)
      .globally()
      .substituteMap(subs)
      .result()

    writeAndLog(outputFile, xmlNode.mkString)
  }

  /**
    * This is workaround for the loading of JSON files using the -Dconfig.file=/path/to/file.json model.
    *
    * The merges the custom third-party options defined in internal-config.json and writes merged
    * application.json file. The merged application.json should be passed to all downstream tools, such as
    * get-status.
    *
    * There might be a better solution to this.
    *
    * @param smrtLinkSystemConfig Path to smrtlink-system-config.json
    * @param internalConfig       Internal third-party config path hocon.json file
    * @param output               output file to write to
    * @return
    */
  def writeApplicationJson(smrtLinkSystemConfig: File,
                           internalConfig: File,
                           output: File): Path = {

    val j1 = loadJson(smrtLinkSystemConfig)
    val j2 = loadJson(internalConfig)

    val jTotal: JsObject = new JsObject(
      j1.asJsObject.fields ++ j2.asJsObject.fields)

    writeAndLog(output, jTotal.prettyPrint.toString)

    output.toPath
  }

  /**
    * 1.  Load and Validate smrtlink-system-config.json
    * 2.  Setup Log to location defined in config JSON (Not Applicable. JVM_OPTS will do this?)
    * 3.  Null host (?) clarify this interface (Unclear what to do here. Set the host to fqdn?)
    * 4.  (write_services_jvm_args) write jvm args (/ROOT_BUNDLE/services-jvm-args)
    * 5.  (write_services_args) write jvm log (/ROOT_BUNDLE/services-log-args)
    * 6.  (update_server_path_in_ui) Update UI api-server.config.json within the Tomcat dir (webapps/ROOT/sl/api-server.config.json)
    * 7.  (update_tomcat) Update Tomcat XML (TOMCAT_ROOT/conf/server.xml) and .keystore file in /ROOT_BUNDLE
    * 8.  (update_sl_ui) Updates wso2-2.0.0/repository/deployment/server/synapse-configs/default/api/_sl-ui_.xml
    * 9.  (update_redirect) write index.jsp to tomcat root
    * 10. (update_user_mgt) Updates wso2-2.0.0/repository/conf/user-mgt.xml
    * 11. (update_jndi_properties) Updates wso2-2.0.0/repository/conf/jndi.properties
    * 12. (update_readonly_userstore) Updates wso2-2.0.0/repository/deployment/server/synapse-configs/default/sequences
    * 13. Copy metrics config (to disable metrics)
    * 14. Configure wso2 to use postgres
    */
  def run(opts: ApplyConfigToolOptions): String = {

    // if templateDir is not provided, a dir "templates" dir within
    // the rootBundleDir
    val rootBundleDir = opts.rootDir

    val templatePath =
      opts.templateDir.getOrElse(opts.rootDir.resolve("templates"))

    val smrtLinkConfigPath =
      rootBundleDir.resolve(ApplyConfigConstants.SL_CONFIG_JSON)

    val smrtLinkConfig = loadSmrtLinkSystemConfig(smrtLinkConfigPath)

    val c = validateConfig(smrtLinkConfig)

    val internalConfig =
      rootBundleDir.resolve(ApplyConfigConstants.SL_INTERNAL_CONFIG_JSON)

    val appConfig =
      rootBundleDir.resolve(ApplyConfigConstants.SL_APP_CONFIG_JSON)

    writeApplicationJson(smrtLinkConfigPath.toFile,
                         internalConfig.toFile,
                         appConfig.toFile)

    val wso2CredentialsPath =
      rootBundleDir.resolve(ApplyConfigConstants.WSO2_CREDENTIALS_JSON)

    val wso2Credentials = loadWso2Credentials(wso2CredentialsPath)

    // If the DNS name is None, resolve the FQDN of the host and log a warning. This interface
    // needs to be well-defined.
    val host = c.smrtflow.server.dnsName match {
      case Some("0.0.0.0") =>
        java.net.InetAddress.getLocalHost.getCanonicalHostName
      case Some(x) => x
      case _ =>
        val fqdnHost = java.net.InetAddress.getLocalHost.getCanonicalHostName
        logger.warn(
          s"Null dnsName set for smrtflow.server.dnsName. Using $fqdnHost")
        fqdnHost
    }

    // Config parameters that are hardcoded
    val defaultLogLevel = "INFO"
    val wso2Port = 8243

    // What's the difference between these?
    val authUrl = new URL(s"https://$host:$wso2Port")
    val smrtLinkUrl = new URL(s"https://$host:$wso2Port")
    // raw SLA port
    val smrtLinkBackEndUrl = new URL(s"http://$host:${c.smrtflow.server.port}")
    val smrtViewUrl = new URL(s"http://$host:${c.pacBioSystem.smrtViewPort}")
    val techSupportUrl: Option[URL] = None

    val resolver = new BundleOutputResolver(rootBundleDir)

    val templateResolver = new TemplateOutputResolver(templatePath)

    // #4
    writeJvmArgs(resolver.jvmArgs.toFile,
                 c.pacBioSystem.smrtLinkServerMemoryMin,
                 c.pacBioSystem.smrtLinkServerMemoryMax)

    // #5
    writeJvmLogArgs(resolver.jvmLogArgs.toFile,
                    c.pacBioSystem.logDir,
                    defaultLogLevel)

    // #6
    updateApiServerConfig(resolver.uiApiServerConfig.toFile,
                          authUrl,
                          c.smrtflow.server.eventUrl,
                          smrtLinkUrl,
                          smrtLinkBackEndUrl,
                          smrtViewUrl,
                          techSupportUrl)

    // #7a
    updateTomcatSetupEnvSh(resolver.tomcatEnvSh.toFile,
                           c.pacBioSystem.tomcatMemory)

    // 7b
    updateTomcatConfig(resolver.tomcatServerXml.toFile,
                       templateResolver.serverXml.toFile,
                       c.pacBioSystem.tomcatPort)

    // #8
    updateUIProxyXml(resolver.uiProxyConfig.toFile,
                     templateResolver.uiProxyXml.toFile,
                     host,
                     c.pacBioSystem.tomcatPort,
                     ApplyConfigConstants.STATIC_FILE_DIR)

    // #9
    updateWso2Redirect(resolver.tomcatIndexJsp.toFile,
                       templateResolver.indexJsp.toFile,
                       host,
                       wso2Port,
                       ApplyConfigConstants.STATIC_FILE_DIR)

    // #10
    updateWso2ConfFile(resolver.userMgtConfig.toFile,
                       templateResolver.userMgtXml.toFile,
                       wso2Credentials.wso2User,
                       wso2Credentials.wso2Password)

    // #11
    updateWso2ConfFile(resolver.jndiPropertiesConfig.toFile,
                       templateResolver.jndiProperties.toFile,
                       wso2Credentials.wso2User,
                       wso2Credentials.wso2Password)

    // #12
    updateReadOnlyUserstore(resolver.wso2SeqDir,
                            templateResolver.readOnlyUserStore.toFile,
                            wso2Credentials.wso2User)

    // #13
    updateMetricsConfig(resolver.metricsXmlConfig, templateResolver.metricsXml)

    // #14
    val dbProps = c.smrtflow.db.properties
    updateMasterDatasources(
      resolver.masterDatasourcesConfig.toFile,
      templateResolver.masterDatasources.toFile,
      // if the db ever ends up on another machine,
      // "localhost" should become dbProps.serverName
      "localhost",
      dbProps.portNumber,
      dbProps.user,
      dbProps.password
    )

    setupWso2LogDir(rootBundleDir, c.pacBioSystem.logDir)

    updateUiAppConfig(
      resolver.uiAppConfig.toFile,
      smrtLinkConfig.pacBioSystem.enableCellReuse.getOrElse(false))

    "Successfully Completed apply-config"
  }
}

object ApplyConfigTool extends CommandLineToolRunner[ApplyConfigToolOptions] {

  val VERSION = "0.3.0"
  val DESCRIPTION =
    "Apply smrtlink-system-config.json to SubComponents of the SMRT Link system"
  val toolId = "smrtflow.tools.apply_config"

  val defaults = ApplyConfigToolOptions(null)

  /**
    * This should do more validation on the required template files
    *
    * @param p Root Path to th bundle directory
    * @return
    */
  def validateIsDir(p: File): Either[String, Unit] = {
    if (Files.isDirectory(p.toPath)) Right(Unit)
    else Left(s"$p must be a directory")
  }

  lazy val parser = new OptionParser[ApplyConfigToolOptions]("apply-config") {

    arg[File]("root-dir")
      .action((x, c) => c.copy(rootDir = x.toPath.toAbsolutePath))
      .validate(validateIsDir)
      .text("Root directory of the SMRT Link Analysis GUI bundle")

    opt[File]("template-dir")
      .action((x, c) => c.copy(templateDir = Some(x.toPath)))
      .validate(validateIsDir)
      .text("Override path to template directory. By default 'template' dir within <ROOT-DIR> will be used")

    opt[Unit]('h', "help") action { (x, c) =>
      showUsage
      sys.exit(0)
    } text "Show Options and exit"

    opt[Unit]("version") action { (x, c) =>
      showVersion
      sys.exit(0)
    } text "Show tool version and exit"

    override def showUsageOnError = false

    // add the shared `--debug` and logging options
    LoggerOptions.add(this.asInstanceOf[OptionParser[LoggerConfig]])
  }

  override def runTool(opts: ApplyConfigToolOptions): Try[String] =
    Try { ApplyConfigUtils.run(opts) }

  // for backward compat with old API
  def run(config: ApplyConfigToolOptions) =
    Left(ToolFailure(toolId, 0, "NOT SUPPORTED"))
}

object ApplyConfigToolApp extends App {
  import ApplyConfigTool._
  runnerWithArgsAndExit(args)
}
