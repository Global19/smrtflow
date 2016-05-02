import java.text.SimpleDateFormat
import java.util.{Calendar, UUID}

import akka.testkit.TestActorRef
import com.pacbio.common.actors.InMemoryUserDaoProvider
import com.pacbio.common.auth._
import com.pacbio.common.dependency.Singleton
import com.pacbio.common.models.{UserRecord, XmlTemplateReader}
import com.pacbio.common.services.{PacBioServiceErrors, ServiceComposer}
import com.pacbio.common.time.{PacBioDateTimeFormat, FakeClockProvider}
import com.pacbio.secondary.smrtlink.actors._
import com.pacbio.secondary.smrtlink.auth.SmrtLinkRoles
import com.pacbio.secondary.smrtlink.models._
import com.pacbio.secondary.smrtlink.services.RunService
import com.pacificbiosciences.pacbiobasedatamodel.SupportedAcquisitionStates
import org.joda.time.{DateTime => JodaDateTime}
import org.specs2.mutable.Specification
import org.specs2.specification.Scope
import spray.http.OAuth2BearerToken
import spray.httpx.SprayJsonSupport._
import spray.routing.{AuthorizationFailedRejection, AuthenticationFailedRejection, Directives}
import spray.testkit.Specs2RouteTest

class RunSpec extends Specification with Directives with Specs2RouteTest with BaseRolesInit with PacBioServiceErrors {
  // Tests must be run in sequence because of shared state in InMemoryHealthDaoComponent
  sequential

  import SmrtLinkJsonProtocols._
  import SmrtLinkRoles._

  val RUN_ID = UUID.randomUUID()
  val RUN_NAME = s"Run-$RUN_ID"
  val RUN_SUMMARY = "Fake Data Model"
  val CREATED_AT = new JodaDateTime(2016, 4, 15, 2, 25, PacBioDateTimeFormat.TIME_ZONE) // "2016-04-15T02:26:00"
  val CREATED_BY = "jsnow"

  def instrumentName(id: String) = s"Inst$id"

  val SUBREAD_ID_1 = UUID.randomUUID()
  val NAME_1 = "WellSample1"
  val SUMMARY_1 = "Well Sample 1"
  val WELL_NAME_1 = "A01"
  val EXTERNAL_RESOURCE_ID_1 = UUID.randomUUID()
  val CONTEXT_ID_1 = s"mSim_${new SimpleDateFormat("yyMMdd_HHmmss").format(Calendar.getInstance().getTime)}"
  val STATUS_1 = SupportedAcquisitionStates.READY
  val INSTRUMENT_ID_1 = 54001.toString
  val MOVIE_MINUTES_1 = 60.0
  val STARTED_AT_1 = new JodaDateTime(2016, 4, 15, 2, 26, PacBioDateTimeFormat.TIME_ZONE) // "2016-04-15T02:26:00"
  val PATH_URI_1 = s"//pbi/collections/xfer-test/$CONTEXT_ID_1/1_A01/$RUN_ID/"

  val SUBREAD_ID_2 = UUID.randomUUID()
  val NAME_2 = "WellSample2"
  val WELL_NAME_2 = "B01"
  val EXTERNAL_RESOURCE_ID_2 = UUID.randomUUID()
  val STATUS_2 = SupportedAcquisitionStates.READY_TO_CALIBRATE
  val MOVIE_MINUTES_2 = 120.0

  val FAKE_RUN_DATA_MODEL = XmlTemplateReader
    .fromStream(getClass.getResourceAsStream("/fake_run_data_model.xml"))
    .globally().substituteAll(
      "{RUN_ID}"                 -> (() => RUN_ID),
      "{RUN_NAME}"               -> (() => RUN_NAME),
      "{RUN_SUMMARY}"            -> (() => RUN_SUMMARY),
      "{CREATED_AT}"             -> (() => CREATED_AT),
      "{CREATED_BY}"             -> (() => CREATED_BY),

      "{SUBREAD_ID_1}"           -> (() => SUBREAD_ID_1),
      "{NAME_1}"                 -> (() => NAME_1),
      "{SUMMARY_1}"              -> (() => SUMMARY_1),
      "{WELL_NAME_1}"            -> (() => WELL_NAME_1),
      "{EXTERNAL_RESOURCE_ID_1}" -> (() => EXTERNAL_RESOURCE_ID_1),
      "{CONTEXT_ID_1}"           -> (() => CONTEXT_ID_1),
      "{STATUS_1}"               -> (() => STATUS_1.value()),
      "{INSTRUMENT_ID_1}"        -> (() => INSTRUMENT_ID_1),
      "{MOVIE_MINUTES_1}"        -> (() => MOVIE_MINUTES_1),
      "{STARTED_AT_1}"           -> (() => STARTED_AT_1),
      "{PATH_URI_1}"             -> (() => PATH_URI_1),

      "{SUBREAD_ID_2}"           -> (() => SUBREAD_ID_2),
      "{NAME_2}"                 -> (() => NAME_2),
      "{WELL_NAME_2}"            -> (() => WELL_NAME_2),
      "{EXTERNAL_RESOURCE_ID_2}" -> (() => EXTERNAL_RESOURCE_ID_2),
      "{STATUS_2}"               -> (() => STATUS_2.value()),
      "{MOVIE_MINUTES_2}"        -> (() => MOVIE_MINUTES_2)
    ).result().mkString

  val READ_USER_LOGIN = "reader"
  val WRITE_USER_1_LOGIN = "root"
  val WRITE_USER_2_LOGIN = "writer2"
  val INVALID_JWT = "invalid.jwt"
  val READ_CREDENTIALS = OAuth2BearerToken(READ_USER_LOGIN)
  val WRITE_CREDENTIALS_1 = OAuth2BearerToken(WRITE_USER_1_LOGIN)
  val WRITE_CREDENTIALS_2 = OAuth2BearerToken(WRITE_USER_2_LOGIN)
  val INVALID_CREDENTIALS = OAuth2BearerToken(INVALID_JWT)

  object TestProviders extends
    ServiceComposer with
    RunServiceActorProvider with
    InMemoryRunDaoProvider with
    InMemoryUserDaoProvider with
    AuthenticatorImplProvider with
    JwtUtilsProvider with
    FakeClockProvider with
    DataModelParserImplProvider {

    // Provide a fake JwtUtils that uses the login as the JWT, and validates every JWT except for invalidJwt.

    override final val jwtUtils: Singleton[JwtUtils] = Singleton(() => new JwtUtils {
      override def getJwt(user: ApiUser): String = user.login
      override def validate(jwt: String): Option[String] = if (jwt == INVALID_JWT) None else Some(jwt)
    })

    override final val defaultRoles = Set.empty[Role]
  }

  val actorRef = TestActorRef[RunServiceActor](TestProviders.runServiceActor())
  val authenticator = TestProviders.authenticator()

  val routes = new RunService(actorRef, authenticator).prefixedRoutes

  TestProviders.userDao().createUser(READ_USER_LOGIN, UserRecord("pass"))
  TestProviders.userDao().createUser(WRITE_USER_1_LOGIN, UserRecord("pass"))
  TestProviders.userDao().addRole(WRITE_USER_1_LOGIN, RUN_DESIGN_WRITE)
  TestProviders.userDao().createUser(WRITE_USER_2_LOGIN, UserRecord("pass"))
  TestProviders.userDao().addRole(WRITE_USER_2_LOGIN, RUN_DESIGN_WRITE)

  trait daoSetup extends Scope {
    TestProviders.runDao().asInstanceOf[InMemoryRunDao].clear()
    TestProviders.runDao().createRun(RunCreate(FAKE_RUN_DATA_MODEL))
  }

  "Run Service" should {
    // TODO(smcclellan): Add more than one run
    "return a list of all runs" in new daoSetup {
      Get("/smrt-link/runs") ~> addCredentials(READ_CREDENTIALS) ~> routes ~> check {
        status.isSuccess must beTrue
        val runs = responseAs[Set[RunSummary]]
        runs.size === 1
        runs.head.uniqueId === RUN_ID
      }
    }

    "return a subset of all runs" in new daoSetup {
      Get(s"/smrt-link/runs?createdBy=$CREATED_BY") ~> addCredentials(READ_CREDENTIALS) ~> routes ~> check {
        status.isSuccess must beTrue
        val runs = responseAs[Set[RunSummary]]
        runs.size === 1
        runs.head.createdBy === Some(CREATED_BY)
      }

      Get("/smrt-link/runs?substring=Fake") ~> addCredentials(READ_CREDENTIALS) ~> routes ~> check {
        status.isSuccess must beTrue
        val runs = responseAs[Set[RunSummary]]
        runs.size === 1
        runs.head.summary === Some(RUN_SUMMARY)
      }

      Get("/smrt-link/runs?substring=Run") ~> addCredentials(READ_CREDENTIALS) ~> routes ~> check {
        status.isSuccess must beTrue
        val runs = responseAs[Set[RunSummary]]
        runs.size === 1
        runs.head.name === RUN_NAME
      }

      Get("/smrt-link/runs?reserved=false") ~> addCredentials(READ_CREDENTIALS) ~> routes ~> check {
        status.isSuccess must beTrue
        val runs = responseAs[Set[RunSummary]]
        runs.size === 1
        runs.head.reserved === false
      }

      Get("/smrt-link/runs?createdBy=foo") ~> addCredentials(READ_CREDENTIALS) ~> routes ~> check {
        status.isSuccess must beTrue
        val runs = responseAs[Set[RunSummary]]
        runs.size === 0
      }

      Get("/smrt-link/runs?substring=foo") ~> addCredentials(READ_CREDENTIALS) ~> routes ~> check {
        status.isSuccess must beTrue
        val runs = responseAs[Set[RunSummary]]
        runs.size === 0
      }

      Get("/smrt-link/runs?reserved=true") ~> addCredentials(READ_CREDENTIALS) ~> routes ~> check {
        status.isSuccess must beTrue
        val runs = responseAs[Set[RunSummary]]
        runs.size === 0
      }
    }

    "return a specific run" in new daoSetup {
      Get(s"/smrt-link/runs/$RUN_ID") ~> addCredentials(READ_CREDENTIALS) ~> routes ~> check {
        status.isSuccess must beTrue
        val run = responseAs[Run]
        // TODO(smcclellan): Check all parsed fields
        run.uniqueId === RUN_ID
        run.name === RUN_NAME
        run.summary === Some(RUN_SUMMARY)
        run.createdAt === Some(CREATED_AT)
        run.createdBy === Some(CREATED_BY)
        run.totalCells === 2
        run.numCellsCompleted === 0
        run.numCellsFailed === 0
      }
    }

    "return a run set of collections" in new daoSetup {
      Get(s"/smrt-link/runs/$RUN_ID/collections") ~> addCredentials(READ_CREDENTIALS) ~> routes ~> check {
        status.isSuccess must beTrue
        val collections = responseAs[Seq[CollectionMetadata]]
        collections.size === 2

        val collect1 = collections.filter(_.uniqueId == SUBREAD_ID_1).head
        collect1.runId === RUN_ID
        collect1.name === NAME_1
        collect1.summary === Some(SUMMARY_1)
        collect1.context === Some(CONTEXT_ID_1)
        collect1.status === STATUS_1
        collect1.instrumentId === Some(INSTRUMENT_ID_1)
        collect1.instrumentName === Some(instrumentName(INSTRUMENT_ID_1))
        collect1.movieMinutes === MOVIE_MINUTES_1
        collect1.startedAt === Some(STARTED_AT_1)
        collect1.completedAt === None
        collect1.terminationInfo === None

        val collect2 = collections.filter(_.uniqueId == SUBREAD_ID_2).head
        collect2.runId === RUN_ID
        collect2.name === NAME_2
        collect2.summary === None
        collect2.context === None
        collect2.status === STATUS_2
        collect2.instrumentId === None
        collect2.instrumentName === None
        collect2.movieMinutes === MOVIE_MINUTES_2
        collect2.startedAt === None
        collect2.completedAt === None
        collect2.terminationInfo === None
      }
    }

    "return a specific collection" in new daoSetup {
      Get(s"/smrt-link/runs/$RUN_ID/collections/$SUBREAD_ID_1") ~> addCredentials(READ_CREDENTIALS) ~> routes ~> check {
        status.isSuccess must beTrue
        val collect1 = responseAs[CollectionMetadata]

        collect1.runId === RUN_ID
        collect1.name === NAME_1
        collect1.summary === Some(SUMMARY_1)
        collect1.context === Some(CONTEXT_ID_1)
        collect1.status === STATUS_1
        collect1.instrumentId === Some(INSTRUMENT_ID_1)
        collect1.instrumentName === Some(instrumentName(INSTRUMENT_ID_1))
        collect1.movieMinutes === MOVIE_MINUTES_1
        collect1.startedAt === Some(STARTED_AT_1)
        collect1.completedAt === None
        collect1.terminationInfo === None
      }
    }

    "create a run" in new daoSetup {
      val newId = UUID.randomUUID()
      val newCreator = "astark"
      val newModel = FAKE_RUN_DATA_MODEL
        .replaceAllLiterally(RUN_ID.toString, newId.toString)
        .replaceAllLiterally(CREATED_BY, newCreator)
      val create = RunCreate(newModel)
      Post("/smrt-link/runs", create) ~> addCredentials(WRITE_CREDENTIALS_1) ~> routes ~> check {
        status.isSuccess must beTrue
        val run = responseAs[RunSummary]
        run.uniqueId === newId
        run.createdBy === Some(newCreator)
        run.reserved === false
      }

      Get(s"/smrt-link/runs/$newId") ~> addCredentials(READ_CREDENTIALS) ~> routes ~> check {
        status.isSuccess must beTrue
        val run = responseAs[Run]
        run.uniqueId === newId
        run.createdBy === Some(newCreator)
        run.reserved === false
        run.dataModel === newModel
      }
    }

    "update a run" in new daoSetup {
      val newStatus = SupportedAcquisitionStates.COMPLETE
      val newModel = FAKE_RUN_DATA_MODEL.replace(STATUS_1.value(), newStatus.value())

      val update1 = RunUpdate(
        dataModel = None,
        reserved = Some(true))

      Post(s"/smrt-link/runs/$RUN_ID", update1) ~> addCredentials(WRITE_CREDENTIALS_2) ~> routes ~> check {
        status.isSuccess must beTrue
        val run = responseAs[RunSummary]
        run.reserved === true
      }

      Get(s"/smrt-link/runs/$RUN_ID") ~> addCredentials(READ_CREDENTIALS) ~> routes ~> check {
        status.isSuccess must beTrue
        val run = responseAs[Run]
        run.reserved === true
        run.dataModel === FAKE_RUN_DATA_MODEL
      }

      val update2 = RunUpdate(
        dataModel = Some(newModel),
        reserved = None)

      Post(s"/smrt-link/runs/$RUN_ID", update2) ~> addCredentials(WRITE_CREDENTIALS_2) ~> routes ~> check {
        status.isSuccess must beTrue
        val run = responseAs[RunSummary]
        run.numCellsCompleted === 1
        run.reserved === true
      }

      Get(s"/smrt-link/runs/$RUN_ID") ~> addCredentials(READ_CREDENTIALS) ~> routes ~> check {
        status.isSuccess must beTrue
        val run = responseAs[Run]
        run.numCellsCompleted === 1
        run.reserved === true
        run.dataModel === newModel
      }

      Get(s"/smrt-link/runs/$RUN_ID/collections/$SUBREAD_ID_1") ~> addCredentials(READ_CREDENTIALS) ~> routes ~> check {
        status.isSuccess must beTrue
        val collection = responseAs[CollectionMetadata]

        collection.uniqueId === SUBREAD_ID_1
        collection.runId === RUN_ID
        collection.context === Some(CONTEXT_ID_1)
        collection.status === newStatus
        collection.instrumentId === Some(INSTRUMENT_ID_1)
        collection.instrumentName === Some(instrumentName(INSTRUMENT_ID_1))
      }
    }

    "delete a run design" in new daoSetup {
      Delete(s"/smrt-link/runs/$RUN_ID") ~> addCredentials(WRITE_CREDENTIALS_1) ~> routes ~> check {
        status.isSuccess must beTrue
      }

      Get(s"/smrt-link/runs/$RUN_ID") ~> addCredentials(READ_CREDENTIALS) ~> routes ~> check {
        status.isSuccess must beFalse
        status.intValue === 404
      }
    }

    // TODO(mskinner): turn these back on when we turn auth back on for these endpoints
    // "reject unauthorized users" in new daoSetup {
    //   Get("/smrt-link/runs") ~> routes ~> check {
    //     handled must beFalse
    //     rejection must beAnInstanceOf[AuthenticationFailedRejection]
    //   }

    //   Get("/smrt-link/runs") ~> addCredentials(INVALID_CREDENTIALS) ~> routes ~> check {
    //     handled must beFalse
    //     rejection must beAnInstanceOf[AuthenticationFailedRejection]
    //   }

    //   val create = RunCreate(<run id="0" name="X">XXX</run>.mkString, "Name", "Summary.")
    //   Post("/smrt-link/runs", create) ~> addCredentials(READ_CREDENTIALS) ~> routes ~> check {
    //     handled must beFalse
    //     rejection === AuthorizationFailedRejection
    //   }

    //   val update = RunUpdate(runDataModel = None, name = None, summary = None, reserved = None)
    //   Post("/smrt-link/runs/0", update) ~> addCredentials(READ_CREDENTIALS) ~> routes ~> check {
    //     handled must beFalse
    //     rejection === AuthorizationFailedRejection
    //   }

    //   Delete("/smrt-link/runs/0", update) ~> addCredentials(READ_CREDENTIALS) ~> routes ~> check {
    //     handled must beFalse
    //     rejection === AuthorizationFailedRejection
    //   }
    // }

    "reject malformed xml" in new daoSetup {
      val create = RunCreate(<foo id="0" name="X">XXX</foo>.mkString)
      Post("/smrt-link/runs", create) ~> addCredentials(WRITE_CREDENTIALS_1) ~> routes ~> check {
        status.intValue === 422
      }

      val update = RunUpdate(dataModel = Some(<run id="0" foo="A">AAA</run>.mkString))
      Post(s"/smrt-link/runs/$RUN_ID", update) ~> addCredentials(WRITE_CREDENTIALS_1) ~> routes ~> check {
        status.intValue === 422
      }
    }
  }
}