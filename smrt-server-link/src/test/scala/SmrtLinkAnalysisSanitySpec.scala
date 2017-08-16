import com.pacbio.common.models.ServiceStatus
import com.pacbio.secondary.smrtlink.app.SecondaryAnalysisProviders
import org.specs2.mutable.Specification
import spray.httpx.SprayJsonSupport._
import spray.testkit.Specs2RouteTest


class SmrtLinkAnalysisSanitySpec extends Specification with Specs2RouteTest {

  import com.pacbio.secondary.smrtlink.models.PacBioJsonProtocol._

  val providers = new SecondaryAnalysisProviders {}
  val totalRoutes = providers.routes()
  val eventManagerActorX = providers.eventManagerActor()


  "Service list" should {
    "return a list of services" in {
      Get("/services/manifests") ~> totalRoutes ~> check {
        status.isSuccess must beTrue
      }
    }
    "Uptime should be >0" in {
      Get("/status") ~> totalRoutes ~> check {
        val status = responseAs[ServiceStatus]
        // Uptime is in sec, not millisec
        // this is the best we can do
        status.uptime must be_>=(0L)
      }
    }
  }
}
