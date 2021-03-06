import com.pacbio.secondary.smrtlink.models.{PacBioDataBundle, ServiceStatus}
import com.pacbio.secondary.smrtlink.app.{SmrtLinkApi, SmrtLinkProviders}
import org.specs2.mutable.Specification
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.testkit.Specs2RouteTest

class PacBioBundleServiceSpec extends Specification with Specs2RouteTest {

  object Api extends SmrtLinkApi {}

  val routes = Api.routes

  import com.pacbio.secondary.smrtlink.jsonprotocols.SmrtLinkJsonProtocols._

  "Bundle Service tests" should {
    "Uptime should be >0" in {
      Get("/status") ~> routes ~> check {
        val status = responseAs[ServiceStatus]
        // Uptime is in sec, not millisec
        // this is the best we can do
        status.uptime must be_>=(0L)
      }
    }
    "Bundle Sanity check" in {
      Get("/smrt-link/bundles") ~> routes ~> check {
        val bundles = responseAs[Seq[PacBioDataBundle]]
        //println(s"All loaded bundles $bundles")
        status.isSuccess must beTrue
      }
    }
    "Get bundle type id 'chemistry' " in {
      Get("/smrt-link/bundles/chemistry") ~> routes ~> check {
        val bundles = responseAs[Seq[PacBioDataBundle]]
        println(s"Example bundles $bundles")
        status.isSuccess must beTrue
      }
    }
    "Get lastest bundle type id 'chemistry' " in {
      Get("/smrt-link/bundles/chemistry/latest") ~> routes ~> check {
        status.isSuccess must beTrue
      }
    }
    "Get Active bundle type id 'chemistry' " in {
      Get("/smrt-link/bundles/chemistry/active") ~> routes ~> check {
        status.isSuccess must beTrue
      }
    }
    "Get upgrade bundle type id 'chemistry' " in {
      Get("/smrt-link/bundles/chemistry/upgrade") ~> routes ~> check {
        status.isSuccess must beTrue
      }
    }
    "Get bundle type id 'chemistry' by version id" in {
      Get("/smrt-link/bundles/chemistry/0.1.2") ~> routes ~> check {
        status.isSuccess must beTrue
      }
    }
    "Get manifest.xml file in 'chemistry'" in {
      Get("/smrt-link/bundles/chemistry/active/files/manifest.xml") ~> routes ~> check {
        status.isSuccess must beTrue
      }
    }
    "Get SampleCalculatorParams.json file in 'chemistry'" in {
      Get(
        "/smrt-link/bundles/chemistry/active/files/SampleCalculatorParams.json") ~> routes ~> check {
        status.isSuccess must beTrue
      }
    }
    "Get PacBioAutomationConstraints.xml file in 'chemistry'" in {
      Get(
        "/smrt-link/bundles/chemistry/active/files/definitions/PacBioAutomationConstraints.xml") ~> routes ~> check {
        status.isSuccess must beTrue
      }
    }
  }
}
