package uk.gov.gds.router.controller

import com.google.inject.Singleton
import uk.gov.gds.router.util.HttpProxy
import uk.gov.gds.router.repository.route.Routes
import uk.gov.gds.router.model.Route
import org.apache.http.client.entity.UrlEncodedFormEntity
import org.apache.http.message.BasicNameValuePair
import org.apache.http.client.methods.{HttpPost, HttpGet}
import scala.collection.JavaConversions._

@Singleton
class RouteController() extends ControllerBase {

  get("/route/*") {
    Routes.load(requestInfo.pathParameter) match {
      case Some(route) => HttpProxy.proxy(new HttpGet(targetUrl(route, requestInfo)))
      case None => halt(404)
    }
  }

  post("/route/*") {
    Routes.load(requestInfo.pathParameter) match {
      case Some(route) => val postRequest = new HttpPost(targetUrl(route, requestInfo))

      val paramsList = requestInfo.requestParameters.map {
        case (param, value) => new BasicNameValuePair(param, value)
      }.toList

      postRequest.setEntity(new UrlEncodedFormEntity(paramsList, "UTF-8"))
      HttpProxy.proxy(postRequest)
      case None => halt(404)
    }
  }

  private def targetUrl(route: Route, request: RequestInfo) =
    "http://".concat(route.application.backend_url.concat(request.targetUrl))
}
