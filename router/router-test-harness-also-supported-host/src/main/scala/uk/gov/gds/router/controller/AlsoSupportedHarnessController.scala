package uk.gov.gds.router.controller

import org.scalatra.ScalatraFilter
import javax.servlet.http.Cookie
import scala.collection.JavaConversions._

class AlsoSupportedHarnessController extends ScalatraFilter {

  before() {
    response.setContentType("text/html")
  }

  get("/a-prefix-route/foo") {
    output("oof!")
  }

  private def output(block: => String) =
    <html>
      <head>
        <title>Test harness</title>
      </head>
      <body>
        {block}
      </body>
    </html>

}
