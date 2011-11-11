package uk.gov.gds.router.util

import org.apache.http.conn.scheme.{PlainSocketFactory, Scheme, SchemeRegistry}
import org.apache.http.conn.ssl.SSLSocketFactory
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager
import org.apache.http.impl.client.DefaultHttpClient
import javax.servlet.http.HttpServletResponse
import uk.gov.gds.router.model.Route
import uk.gov.gds.router.controller.RequestInfo
import org.apache.http.client.methods.{HttpPost, HttpGet, HttpUriRequest}
import org.apache.http.message.BasicNameValuePair
import org.apache.http.client.entity.UrlEncodedFormEntity
import collection.JavaConversions._

object HttpProxy extends Logging {

  val schemeRegistry = new SchemeRegistry
  val connectionManager = new ThreadSafeClientConnManager(schemeRegistry)
  val httpClient = new DefaultHttpClient(connectionManager)

  schemeRegistry.register(new Scheme("http", 80, PlainSocketFactory.getSocketFactory))
  schemeRegistry.register(new Scheme("https", 443, SSLSocketFactory.getSocketFactory))
  connectionManager.setMaxTotal(300)
  connectionManager.setDefaultMaxPerRoute(100)

  def get(route: Route)(implicit requestInfo: RequestInfo, response: HttpServletResponse) {
    proxy(new HttpGet(targetUrl(route)))
  }

  def post(route: Route)(implicit requestInfo: RequestInfo, response: HttpServletResponse) {
    val postRequest = new HttpPost(targetUrl(route))
    val paramsList = requestInfo.multiParams.map {
      case (param, values) =>
        values map {
          value =>
            new BasicNameValuePair(param, value)
        }
    }.flatten.toList

    postRequest.setEntity(new UrlEncodedFormEntity(paramsList, "UTF-8"))
    HttpProxy.proxy(postRequest)
  }

  private def proxy(message: HttpUriRequest)(implicit response: HttpServletResponse) {
    logger.info("Proxying {} {}", message.getMethod, message.getURI)
    httpClient.execute(message).getEntity.writeTo(response.getOutputStream)
  }

  private def targetUrl(route: Route)(implicit requestInfo: RequestInfo) =
    "http://".concat(route.application.backend_url.concat(requestInfo.targetUrl))
}