package uk.gov.gds.router.controller

import com.google.inject.Singleton
import uk.gov.gds.router.repository.route.Routes
import org.apache.http.protocol.HTTP
import javax.servlet.http.HttpServletResponse
import org.apache.http.message.BasicNameValuePair
import org.apache.http.client.methods.{HttpUriRequest, HttpPost, HttpGet}
import org.apache.http.HttpResponse
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager
import org.apache.http.impl.client.DefaultHttpClient
import org.apache.http.params.{HttpConnectionParams, BasicHttpParams}
import org.apache.http.client.params.HttpClientParams
import org.apache.http.conn.ssl.SSLSocketFactory
import org.apache.http.conn.scheme.{Scheme, PlainSocketFactory, SchemeRegistry}
import uk.gov.gds.router.model.Route
import uk.gov.gds.router.management.ApplicationMetrics.time
import org.apache.http.client.entity.UrlEncodedFormEntity
import collection.JavaConversions._

@Singleton
class RouteController() extends ControllerBase {
  private val httpClient = configureHttpClient()

  private val requestHeadersToFilter = List(
    HTTP.TRANSFER_ENCODING,
    HTTP.CONTENT_LEN,
    HTTP.TARGET_HOST)

  private val responseHeaderWhitelist = List(
    "Cache-Control",
    "Content-Type",
    "ETag",
    "Expires",
    "Last-Modified",
    "Location",
    "Set-Cookie",
    "Vary",
    "WWW-Authenticate"
  )

  get("/route/*") {
    Routes.load(requestInfo.pathParameter) match {
      case Some(route) => get(route)
      case None => halt(404)
    }
  }

  post("/route/*") {
    Routes.load(requestInfo.pathParameter) match {
      case Some(route) => post(route)
      case None => halt(404)
    }
  }

  private def get(route: Route)(implicit requestInfo: RequestInfo, response: HttpServletResponse) {
    time(route, proxy(new HttpGet(targetUrl(route))))
  }

  private def post(route: Route)(implicit requestInfo: RequestInfo, response: HttpServletResponse) {
    val postRequest = new HttpPost(targetUrl(route))

    val params = requestInfo.multiParams.map {
      case (param, values) => values.map(new BasicNameValuePair(param, _))
    }

    postRequest.setEntity(new UrlEncodedFormEntity(params.flatten.toList, "UTF-8"))
    time(route, proxy(postRequest))
  }

  private def proxy(request: HttpUriRequest)(implicit requestInfo: RequestInfo, clientResponse: HttpServletResponse) {
    processRequestHeaders(requestInfo, request)
    generateResponse(httpClient.execute(request), clientResponse, request)
  }

  private def processRequestHeaders(requestInfo: RequestInfo, request: HttpUriRequest) {
    requestInfo.headers.filter(h => !requestHeadersToFilter.contains(h._1)).foreach {
      case (name, value) => {
        request.addHeader(name, value)
      }
    }

    request.addHeader("X-GovUK-Router-Request", "true")
    requestInfo.headers.filter(h => h._1 == HTTP.TARGET_HOST).foreach(h => request.addHeader("X-Forwarded-Host", h._2))
  }

  private def generateResponse(targetResponse: HttpResponse, clientResponse: HttpServletResponse, request: HttpUriRequest) {
    val statusCode = targetResponse.getStatusLine.getStatusCode

    if (statusCode >= 404) {
      logger.warn("Error recieved from backend server " + statusCode)
      halt(statusCode)
    }
    else {
      clientResponse.setStatus(statusCode)

      targetResponse.getAllHeaders
        .filter(h => responseHeaderWhitelist.contains(h.getName))
        .foreach(h => clientResponse.setHeader(h.getName, h.getValue))

      logger.info("Proxy response " + request.getMethod + " " + request.getURI + " => " + statusCode)

      Option(targetResponse.getEntity) match {
        case Some(entity) => entity.writeTo(clientResponse.getOutputStream)
        case _ => logger.trace("Router detected response with no entity {} {}", targetResponse.getAllHeaders, statusCode)
      }
    }
  }

  private def targetUrl(route: Route)(implicit request: RequestInfo) =
    "http://".concat(route.application.backend_url.concat(request.targetUrl))

  private def configureHttpClient() = {
    val schemeRegistry = new SchemeRegistry
    val connectionManager = new ThreadSafeClientConnManager(schemeRegistry)
    val httpClient = new DefaultHttpClient(connectionManager)
    val httpParams = new BasicHttpParams()

    HttpConnectionParams.setConnectionTimeout(httpParams, 5000);
    HttpConnectionParams.setSoTimeout(httpParams, 5000);
    HttpClientParams.setRedirecting(httpParams, false)
    schemeRegistry.register(new Scheme("http", 80, PlainSocketFactory.getSocketFactory))
    schemeRegistry.register(new Scheme("https", 443, SSLSocketFactory.getSocketFactory))
    connectionManager.setMaxTotal(300)
    connectionManager.setDefaultMaxPerRoute(100)
    httpClient.setParams(httpParams)
    httpClient
  }
}
