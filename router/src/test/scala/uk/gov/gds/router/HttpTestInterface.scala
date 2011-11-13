package uk.gov.gds.router

import org.apache.http.util.EntityUtils
import org.apache.http.client.methods._
import org.apache.http.message.BasicNameValuePair
import org.apache.http.client.entity.UrlEncodedFormEntity
import scala.collection.JavaConversions._
import org.apache.http.{HttpResponse, HttpEntityEnclosingRequest}
import org.apache.http.params.BasicHttpParams
import org.apache.http.cookie.Cookie
import org.apache.http.impl.cookie.BasicClientCookie
import util.Logging
import java.util.Date
import org.apache.http.client.params.{CookiePolicy, HttpClientParams}
import org.apache.http.impl.client.{BasicCookieStore, DefaultHttpClient}

trait HttpTestInterface extends Logging {

  protected val cookieStore = new BasicCookieStore

  private type PutOrPost = HttpEntityEnclosingRequest with HttpUriRequest
  private val httpClient = new DefaultHttpClient()

  val httpParams = new BasicHttpParams()

  HttpClientParams.setRedirecting(httpParams, false)
  HttpClientParams.setCookiePolicy(httpParams, CookiePolicy.RFC_2109);
  httpClient.setParams(httpParams)
  httpClient.setCookieStore(cookieStore)

  def buildUrl(path: String): String

  def get(url: String, cookies: Map[String, String] = Map.empty) = {
    val httpGet = new HttpGet(buildUrl(url))
    handleCookies(cookies)
    Response(httpGet)
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

  case class Response(status: Int, body: String, cookies: List[Cookie])

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
      cookies = cookieStore.getCookies.toList
    )
  }

}