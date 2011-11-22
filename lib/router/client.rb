require 'router/errors'
require 'router/http_client'
require 'router/routes'
require 'router/applications'

module Router
  class Client
    def initialize(http_client = nil)
      @http_client = http_client || default_http_client
    end
    
    def default_http_client
      HttpClient.new("http://router.cluster/router")
    end

    def routes
      Router::Routes.new(@http_client)
    end

    def applications
      Router::Applications.new(@http_client)
    end
  end
end