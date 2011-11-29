require 'router/client'

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
      @backend_url = backend_url
    end

    def register!
      @router_client.applications.update(application_id: @id, backend_url: @backend_url)
    end
    
    def ensure_prefix_route(incoming_path)
      @router_client.routes.update(application_id: @id, route_type: :prefix, incoming_path: incoming_path)
    end

    def ensure_full_route(incoming_path)
      @router_client.routes.update(application_id: @id, route_type: :full, incoming_path: incoming_path)
    end
  end
end