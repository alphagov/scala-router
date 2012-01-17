package uk.gov.gds.router.management

import com.google.inject.Singleton
import com.gu.management._
import request.RequestLoggingFilter
import uk.gov.gds.router.repository.application.Applications
import uk.gov.gds.router.model.{Route, Application}
import java.util.concurrent.ConcurrentHashMap
import uk.gov.gds.router.util.Logging
import javax.servlet.http.HttpServletRequest

@Singleton
class RouterRequestLoggingFilter extends RequestLoggingFilter(metric = Requests, shouldLogParametersOnNonGetRequests = true)

@Singleton
class RouterManagementFilter extends ManagementFilter {
  lazy val pages = List(new ApplicationStatusPage, new StatsResetPage)
}

object Requests extends TimingMetric(
  group = "cache.cluster",
  name = "router-requests",
  title = "Router requests",
  description = "Incoming router requests")

object ApplicationMetrics extends Logging {

  private val metrics = new ConcurrentHashMap[Application, TimingMetric]()

  def all = Seq(Requests) ++ Applications.all.map(timer(_))

  def time[A](route: Route, block: => A) = timer(route.application).measure(block)

  private def timer(app: Application) = Option(metrics.get(app)) match {
    case Some(metric) =>
      metric
    case None =>
      val metric = new TimingMetric(
        group = "cache.cluster",
        name = app.id,
        title = app.id,
        description = "Incoming application requests")

      metrics.put(app, metric)
      metric
  }
}

class ApplicationStatusPage extends JsonManagementPage {
  val path = "/management/status"

  def jsonObj = ApplicationMetrics.all.map(_.asJson)
}

class StatsResetPage extends ManagementPage {
  val path = "/management/status/reset"

  def get(req: HttpServletRequest) = {
    ApplicationMetrics.all.foreach(_.reset())

    HtmlResponse(
      <html>
        <head>
          <title>Stats Reset</title>
        </head>
        <body>
          <p>Router stats reset on this node</p>
        </body>
      </html>
    )
  }
}

