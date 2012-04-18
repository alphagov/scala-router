require 'router/client'
require 'uri'

class Router
  def initialize(router_endpoint_url, logger=nil)
    @router_client = Router::Client.new(router_endpoint_url, logger)
  end

  def application(application_id, backend_url)
    application = Application.new(@router_client, application_id, backend_url)
    application.register!
    yield application if block_given?
    application
  end

  class Application
    def initialize(router_client, id, backend_url)
      @router_client = router_client
      @id = id
      @backend_location = url_without_scheme(backend_url)
    end

    def register!
      @router_client.applications.update(application_id: @id, backend_url: @backend_location)
    end

    def ensure_prefix_route(incoming_path)
      @router_client.routes.update(application_id: @id, route_type: :prefix, incoming_path: incoming_path)
    end

    def ensure_full_route(incoming_path)
      @router_client.routes.update(application_id: @id, route_type: :full, incoming_path: incoming_path)
    end

    private

    def url_without_scheme(url)
      parsed_url = URI.parse(url)
      if parsed_url.scheme
        "#{parsed_url.host}:#{parsed_url.port}#{parsed_url.path}"
      else
        url
      end
    end
  end
end