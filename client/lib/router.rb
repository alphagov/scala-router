require 'router/http_client'

class Router

  def initialize(router_endpoint_url, logger=nil)
    @http_client = Router::HttpClient.new(router_endpoint_url)
  end

  def reinitialise
    @http_client.get("/reinitialise")
  end

  def create_application(application_name, backend_url)
    @http_client.put("/applications/#{application_name}", {backend_url: url_without_scheme(backend_url)})
  end

  def update_application(application_name, backend_url)
    @http_client.put("/applications/#{application_name}", {backend_url: url_without_scheme(backend_url)})
  end

  def get_application(application_name)
    @http_client.get("/applications/#{application_name}")
  end
  
  def delete_application(application_name)
    @http_client.delete("/applications/#{application_name}")
  end 

  def create_route(route, route_type, application_name)
    @http_client.put("/routes/#{route}", { route_type: route_type, application_id: application_name })
  end

  def get_route(route)
    @http_client.get("/routes/#{route}")
  end

  def delete_route(route)
    @http_client.delete("/routes/#{route}")
  end
 
  def url_without_scheme(url)
    parsed_url = URI.parse(url)
      if parsed_url.scheme
        "#{parsed_url.host}:#{parsed_url.port}#{parsed_url.path}"
      else
        url
      end
  end
end
