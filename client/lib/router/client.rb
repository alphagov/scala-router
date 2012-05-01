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

    def initialize *args
      if args[0].kind_of? Hash
        options = args[0]
        self.http_client = HttpClient.new options[:router_endpoint_url] || default_router_endpoint_url
        self.logger = options[:logger] || NullLoger.instance #or perhaps NullLogger????
      else
        logger = args[1] || NullLogger.instance
        logger.warn "Positional argumentsto Router::Client are deprecated."
        self.http_client = HttpClient.new args[0] || default_router_endpoint_url
        self.logger = logger
      end
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
