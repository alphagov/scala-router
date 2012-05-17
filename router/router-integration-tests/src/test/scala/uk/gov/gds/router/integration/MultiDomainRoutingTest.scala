package uk.gov.gds.router.integration

import uk.gov.gds.router.configuration.RouterConfig

class MultiDomainRoutingTest
  extends RouterIntegrationTestSetup {

  test("hosts map correctly in router config"){
    var hostName = "www.preview.alphagov.co.uk"
    RouterConfig.getPrefixForHost(hostName) should be("/govuk/")

    hostName = "www.gov.uk"
    RouterConfig.getPrefixForHost(hostName) should be("/govuk/")

    hostName = "www.direct.gov.uk"
    RouterConfig.getPrefixForHost(hostName) should be("/directgov/")

    hostName = "www.businesslink.gov.uk"
    RouterConfig.getPrefixForHost(hostName) should be("/businesslink/")
  }

  test("host defaults to /govuk/ if one is not present in request") {
    val hostName = ""
    RouterConfig.getPrefixForHost(hostName) should be("/govuk/")
  }

  test("if non-existant host is provided, an exception is thrown") {
    val hostName = "non-existent hostname"
    RouterConfig.getPrefixForHost(hostName) should be("/govuk/")
  }

  //todo what do we want the non-existent host behaviour to be? above or below?
//    test("if non-existant host is provided, an exception is thrown") {
//    val hostName = "non-existent hostname"
//    intercept[RuntimeException] {
//      val result = RouterConfig.getPrefixForHost(hostName)
//    }
//  }
}
