package uk.gov.gds.router.util

import org.apache.http.conn.scheme.{PlainSocketFactory, Scheme, SchemeRegistry}
import org.apache.http.conn.ssl.SSLSocketFactory
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager
import org.apache.http.impl.client.DefaultHttpClient
import javax.servlet.http.{HttpServletResponse, HttpServletRequest}
import org.apache.http.client.methods.HttpGet
import org.apache.http.client.methods.HttpPost
import uk.gov.gds.router.model.Route
import scala.collection.JavaConversions._
import org.apache.http.client.entity.UrlEncodedFormEntity
import org.apache.http.message.BasicNameValuePair
import uk.gov.gds.router.controller.RequestInfo

object HttpProxy extends Logging {

  val schemeRegistry = new SchemeRegistry
  schemeRegistry.register(new Scheme("http", 80, PlainSocketFactory.getSocketFactory))
  schemeRegistry.register(new Scheme("https", 443, SSLSocketFactory.getSocketFactory))

  val connectionManager = new ThreadSafeClientConnManager(schemeRegistry)
  connectionManager.setMaxTotal(300)
  connectionManager.setDefaultMaxPerRoute(100)

  val httpClient = new DefaultHttpClient(connectionManager)

  def proxyGet(route: Route, requestInfo: RequestInfo, response: HttpServletResponse) {
    val targetUrl = "http://".concat(route.application.backend_url.concat(requestInfo.targetUrl))
    val getRequest = new HttpGet(targetUrl)

    logger.info("Proxying GET {}", targetUrl)
    httpClient.execute(getRequest).getEntity.writeTo(response.getOutputStream)
  }

  def proxyPost(route: Route, requestInfo: RequestInfo, response: HttpServletResponse) {
    val targetUrl = "http://".concat(route.application.backend_url.concat(requestInfo.targetUrl))
    val postRequest = new HttpPost(targetUrl)

    val paramsList = requestInfo.requestParameters.map {
      case (param, value) => new BasicNameValuePair(param, value)
    }.toList

    postRequest.setEntity(new UrlEncodedFormEntity(paramsList, "UTF-8"))

    logger.info("Proxying POST {}", targetUrl)
    httpClient.execute(postRequest).getEntity.writeTo(response.getOutputStream)
  }


}