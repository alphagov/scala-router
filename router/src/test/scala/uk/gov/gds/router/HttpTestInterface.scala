package uk.gov.gds.router

import org.apache.http.util.EntityUtils
import org.apache.http.impl.client.{BasicCookieStore, DefaultHttpClient}
import org.apache.http.client.methods._
import org.apache.http.message.BasicNameValuePair
import org.apache.http.client.entity.UrlEncodedFormEntity
import scala.collection.JavaConversions._
import org.apache.http.{HttpResponse, HttpEntityEnclosingRequest}
import org.apache.http.params.BasicHttpParams
import org.apache.http.client.params.HttpClientParams

trait HttpTestInterface {

  private type PutOrPost = HttpEntityEnclosingRequest with HttpUriRequest
  private val httpClient = new DefaultHttpClient()
  val httpParams = new BasicHttpParams()

  HttpClientParams.setRedirecting(httpParams, false)
  httpClient.setParams(httpParams)
  httpClient.setCookieStore(CookieStore.requestCookies)

  def buildUrl(path: String): String

  def get(url: String) = Response(new HttpGet(buildUrl(url)))

  def delete(url: String) = Response(new HttpDelete(buildUrl(url)))

  def post(url: String, params: Map[String, String] = Map.empty) = Response(new HttpPost(buildUrl(url)), params)

  def put(url: String, params: Map[String, String] = Map.empty) = Response(new HttpPut(buildUrl(url)), params)

  case class Response(status: Int, body: String, rawResponse: HttpResponse)

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
      rawResponse = httpResponse
    )
  }

  object CookieStore {
    val requestCookies = new BasicCookieStore()
  }

}