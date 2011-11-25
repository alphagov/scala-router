package uk.gov.gds.router.controller

import javax.servlet.http.HttpServletRequest
import org.scalatra.ScalatraKernel
import scala.collection.JavaConversions._
import uk.gov.gds.router.util.Logging

case class RequestInfo(pathParameter: String,
                       targetUrl: String,
                       headers: List[(String, String)],
                       multiParams: ScalatraKernel.MultiParams,
                       requestParameters: Map[String, String],
                       queryString: Option[String])

object RequestInfo extends Logging {

  def apply(request: HttpServletRequest,
            params: Map[String, String],
            multiParams: ScalatraKernel.MultiParams): RequestInfo = {

    def unslashed(s: String) = if (s.startsWith("/")) s.replaceFirst("/", "") else s
    def slashed(s: String) = if (!s.startsWith("/")) "/" + s else s

    val queryString = Option(request.getQueryString)
    val requestParams = params - "splat"
    val targetPath = unslashed(multiParams("splat").mkString("/"))

    val targetUrl = queryString match {
      case Some(query) => targetPath.concat("?").concat(query)
      case None => targetPath
    }

    RequestInfo(
      pathParameter = targetPath,
      targetUrl = slashed(targetUrl),
      headers = extractHeaders(request),
      queryString = queryString,
      multiParams = multiParams - "splat",
      requestParameters = requestParams)
  }

  def extractHeaders(request: HttpServletRequest) = {
    val headers = request.getHeaderNames map {
      headerNameObj =>
        val headerName = headerNameObj.asInstanceOf[String]
        (headerName, request.getHeader(headerName))
    }

    headers.toList
  }
}