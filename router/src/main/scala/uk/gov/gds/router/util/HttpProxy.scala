package uk.gov.gds.router.util

import org.apache.http.conn.scheme.{PlainSocketFactory, Scheme, SchemeRegistry}
import org.apache.http.conn.ssl.SSLSocketFactory
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager
import org.apache.http.impl.client.DefaultHttpClient
import javax.servlet.http.HttpServletResponse
import org.apache.http.client.methods.HttpUriRequest

object HttpProxy extends Logging {

  val schemeRegistry = new SchemeRegistry
  val connectionManager = new ThreadSafeClientConnManager(schemeRegistry)
  val httpClient = new DefaultHttpClient(connectionManager)

  schemeRegistry.register(new Scheme("http", 80, PlainSocketFactory.getSocketFactory))
  schemeRegistry.register(new Scheme("https", 443, SSLSocketFactory.getSocketFactory))
  connectionManager.setMaxTotal(300)
  connectionManager.setDefaultMaxPerRoute(100)

  def proxy(message: HttpUriRequest)(implicit response: HttpServletResponse) {
    logger.info("Proxying {} {}", message.getMethod, message.getURI)
    httpClient.execute(message).getEntity.writeTo(response.getOutputStream)
  }
}