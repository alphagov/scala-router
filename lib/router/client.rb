require 'router/errors'
require 'router/http_client'
require 'router/routes'
require 'router/applications'

module Router
  class Client
    def initialize(base_url = "http://router.cluster")
      @http_client = HttpClient.new(base_url)
    end

    def routes
      Router::Routes.new(@http_client)
    end

    def applications
      Router::Applications.new(@http_client)
    end
  end
end