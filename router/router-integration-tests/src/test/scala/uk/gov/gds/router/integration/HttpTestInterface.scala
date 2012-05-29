package uk.gov.gds.router.integration

import org.apache.http.util.EntityUtils
import org.apache.http.client.methods._
import org.apache.http.message.BasicNameValuePair
import org.apache.http.client.entity.UrlEncodedFormEntity
import scala.collection.JavaConversions._
import org.apache.http.{HttpResponse, HttpEntityEnclosingRequest}
import org.apache.http.params.BasicHttpParams
import org.apache.http.cookie.Cookie
import org.apache.http.impl.cookie.BasicClientCookie
import java.util.Date
import org.apache.http.client.params.{CookiePolicy, HttpClientParams}
import org.apache.http.impl.client.{BasicCookieStore, DefaultHttpClient}
import uk.gov.gds.router.util.Logging


trait HttpTestInterface extends Logging  {
  private type PutOrPost = HttpEntityEnclosingRequest with HttpUriRequest

  protected val cookieStore = new BasicCookieStore
  private val httpClient = configureHttpClient

  def buildUrl(path: String): String

  def head(url: String, cookies: Map[String, String] = Map.empty) = {
    val httpHead = new HttpHead(buildUrl(url))
    handleCookies(cookies)
    Response(httpHead)
  }

  def get(url: String, cookies: Map[String, String] = Map.empty) = {
    val remote = buildUrl(url)
    val httpGet = new HttpGet(remote)
    handleCookies(cookies)
    val response = Response(httpGet)

    response
  }

  def delete(url: String) = Response(new HttpDelete(buildUrl(url)))

  def post(url: String, params: Map[String, String] = Map.empty, cookies: Map[String, String] = Map.empty) = {
    val httpPost: HttpPost = new HttpPost(buildUrl(url))
    handleCookies(cookies)
    Response(httpPost, params)
  }

  def put(url: String, params: Map[String, String] = Map.empty) = Response(new HttpPut(buildUrl(url)), params)

  private def handleCookies(cookies: Map[String, String]) {
    val cookieList = cookies.map {
      case (k, v) =>
        val cookie = new BasicClientCookie(k, v)
        cookie.setPath("/")
        cookie.setExpiryDate(new Date(System.currentTimeMillis() + 5000))
        cookie
    }
    cookieList.foreach(cookieStore.addCookie(_))
  }

  private def configureHttpClient = {
    val httpClient = new DefaultHttpClient()
    val httpParams = new BasicHttpParams()

    HttpClientParams.setRedirecting(httpParams, false)
    HttpClientParams.setCookiePolicy(httpParams, CookiePolicy.RFC_2109);
    httpClient.setParams(httpParams)
    httpClient.setCookieStore(cookieStore)
    httpClient
  }

  case class Response(status: Int, body: String, cookies: List[Cookie], headers: List[Header])

  case class Header(name: String, value: String)

  object Response {

    def apply(request: PutOrPost, params: Map[String, String] = Map.empty): Response = {
      val paramsList = params.map {
        case (param, value) => new BasicNameValuePair(param, value)
      }.toList

      request.setEntity(new UrlEncodedFormEntity(paramsList, "UTF-8"))
      Response(request)
    }

    def apply(request: HttpUriRequest): Response = Response(httpClient.execute(request))

    def apply(httpResponse: HttpResponse): Response = Response(
      status = httpResponse.getStatusLine.getStatusCode,
      body = Option(httpResponse.getEntity) match {
        case Some(response) => EntityUtils.toString(response, "UTF-8")
        case _ => ""
      },
      cookies = cookieStore.getCookies.toList,
      headers = httpResponse.getAllHeaders().map {
        h => Header(h.getName, h.getValue)
      }.toList
    )
  }

}
