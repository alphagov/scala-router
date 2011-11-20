package uk.gov.gds.router.management

import com.google.inject.Singleton
import com.gu.management._
import com.gu.management.ManagementPage
import com.gu.management.Metric
import request.RequestLoggingFilter
import javax.servlet.http.HttpServletRequest
import uk.gov.gds.router.repository.application.Applications
import uk.gov.gds.router.model.{Route, Application}
import java.util.concurrent.ConcurrentHashMap

@Singleton
class RouterRequestLoggingFilter extends RequestLoggingFilter(metric = Requests, shouldLogParametersOnNonGetRequests = true)

@Singleton
class RouterManagementFilter extends ManagementFilter {
  lazy val pages = List(new ApplicationStatusPage(List(Requests)))
}

object Requests extends TimingMetric("requests")

object ApplicationMetrics {

  private val metrics = new ConcurrentHashMap[Application, TimingMetric]()

  def all = Applications.all.map(timer(_))

  def time[T](route: Route, block: => T) = timer(route.application).measure(block)

  private def timer(app: Application) = Option(metrics.get(app)) match {
    case Some(metric) => metric
    case None =>
      val metric = new TimingMetric(app.id)
      metrics.put(app, metric)
      metric
  }
}

class ApplicationStatusPage(metrics: Seq[Metric]) extends ManagementPage {
  val path = "/management/status"

  def get(req: HttpServletRequest) = XmlResponse(
    <status>
      {metrics map {_.toXml}}
      <applications>
      {ApplicationMetrics.all map { _.toXml }}
    </applications>
    </status>)
}

