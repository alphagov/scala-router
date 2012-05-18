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

  test("if no host is provided, an excetion is thrown") {
    val hostName = ""
    val thrown = intercept[Exception] {
      RouterConfig.getPrefixForHost(hostName)
    }
    thrown.getMessage should be("Can't find setting: host-")
  }

  test("if non-existant host is provided, an exception is thrown") {
    val hostName = "non-existent hostname"
    val thrown = intercept[Exception] {
      RouterConfig.getPrefixForHost(hostName)
    }
    thrown.getMessage should be("Can't find setting: host-non-existent hostname")
  }
}
