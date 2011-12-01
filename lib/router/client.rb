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

    attr_accessor :logger
    private :logger=, :logger

    def initialize router_endpoint_url = nil, logger = nil
      self.http_client = HttpClient.new(router_endpoint_url || default_router_endpoint_url)
      self.logger = logger || NullLogger.instance
    end

    def default_router_endpoint_url
      "http://router.cluster:8080/router"
    end

    def routes
      logger.debug "Asked for a route manager"
      Router::RouteCollection.new(http_client, logger)
    end

    def applications
      logger.debug "Asked for an application manager"
      Router::ApplicationCollection.new(http_client, logger)
    end
  end
end
