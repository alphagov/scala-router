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
import org.apache.http.client.params.HttpClientParams
import org.apache.http.params.{HttpConnectionParams, BasicHttpParams}
import uk.gov.gds.router.management.ApplicationMetrics.time
import org.apache.http.protocol.HTTP

object HttpProxy extends Logging {

  private val httpClient = configureHttpClient

  private val requestHeadersToFilter = List(
    HTTP.TRANSFER_ENCODING,
    HTTP.CONTENT_LEN,
    HTTP.TARGET_HOST)

  def get(route: Route)(implicit requestInfo: RequestInfo, response: HttpServletResponse) {
    time(route, proxy(new HttpGet(targetUrl(route))))
  }

  def post(route: Route)(implicit requestInfo: RequestInfo, response: HttpServletResponse) {
    val postRequest = new HttpPost(targetUrl(route))

    val params = requestInfo.multiParams.map {
      case (param, values) => values.map(new BasicNameValuePair(param, _))
    }

    postRequest.setEntity(new UrlEncodedFormEntity(params.flatten.toList, "UTF-8"))
    time(route, proxy(postRequest))
  }

  private def proxy(message: HttpUriRequest)(implicit requestInfo: RequestInfo, clientResponse: HttpServletResponse) {
    logger.info("Proxying {} {}", message.getMethod, message.getURI)

    requestInfo.headers.filter(h => !requestHeadersToFilter.contains(h._1)).foreach {
      case (name, value) => message.addHeader(name, value)
    }

    val targetResponse = httpClient.execute(message)
    clientResponse.setStatus(targetResponse.getStatusLine.getStatusCode)
    targetResponse.getAllHeaders.foreach(h => clientResponse.setHeader(h.getName, h.getValue))
    targetResponse.getEntity.writeTo(clientResponse.getOutputStream)
  }

  private def targetUrl(route: Route)(implicit request: RequestInfo) =
    "http://".concat(route.application.backend_url.concat(request.targetUrl))

  private def configureHttpClient = {
    val schemeRegistry = new SchemeRegistry
    val connectionManager = new ThreadSafeClientConnManager(schemeRegistry)
    val httpClient = new DefaultHttpClient(connectionManager)
    val httpParams = new BasicHttpParams()

    HttpConnectionParams.setConnectionTimeout(httpParams, 2000);
    HttpConnectionParams.setSoTimeout(httpParams, 2000);
    HttpClientParams.setRedirecting(httpParams, false)
    schemeRegistry.register(new Scheme("http", 80, PlainSocketFactory.getSocketFactory))
    schemeRegistry.register(new Scheme("https", 443, SSLSocketFactory.getSocketFactory))
    connectionManager.setMaxTotal(300)
    connectionManager.setDefaultMaxPerRoute(100)
    httpClient.setParams(httpParams)
    httpClient
  }
}