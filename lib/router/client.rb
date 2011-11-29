require 'net/http'
require 'ostruct'
require 'json'
require 'null_logger'

require 'router/restful_resource_collection'
require 'router/errors'
require 'router/http_client'
require 'router/route_collection'
require 'router/application_collection'

class Router
  class Client
    attr_accessor :http_client
    private :http_client=, :http_client

    def initialize(http_client = nil)
      self.http_client = http_client || default_http_client
    end

    def default_http_client
      HttpClient.new("http://router.cluster/router")
    end

    def routes
      Router::RouteCollection.new(http_client)
    end

    def applications
      Router::ApplicationCollection.new(http_client)
    end
  end
end
