package uk.gov.gds.router.controller

import javax.servlet.http.HttpServletRequest
import org.scalatra.ScalatraKernel

case class RequestInfo(pathParameter: String,
                       targetUrl: String,
                       requestParameters: Map[String, String],
                       queryString: Option[String])

object RequestInfo {

  def apply(request: HttpServletRequest,
            params: Map[String, String],
            multiParams: ScalatraKernel.MultiParams): RequestInfo = {
    val queryString = Option(request.getQueryString)
    val requestParams = params - "splat"
    val targetPath = unslashed(multiParams("splat").mkString("/"))

    val targetUrl = queryString match {
      case Some(query) => targetPath.concat("?").concat(query)
      case None => targetPath
    }

    RequestInfo(pathParameter = targetPath,
      targetUrl = slashed(targetUrl),
      queryString = queryString,
      requestParameters = requestParams)
  }

  private def unslashed(string: String) = if (string.startsWith("/"))
    string.replaceFirst("/", "")
  else
    string

  private def slashed(string: String) =
    if (!string.startsWith("/"))
      "/" + string
    else
      string
}