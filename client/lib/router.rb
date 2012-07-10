require 'router/http_client'
require 'active_support/core_ext/hash'

class Router

  class ServerError < StandardError; end

  def initialize(router_endpoint_url="http://router.cluster:8080", logger=nil)
    @http_client = Router::HttpClient.new(router_endpoint_url, logger)
  end

  def reinitialise
    @http_client.get("/reinitialise")
  end

  def create_application(application_name, backend_url)
    response = @http_client.put("/applications/#{application_name}", {backend_url: url_without_scheme(backend_url)})

    format_response_for response
  end

  def update_application(application_name, backend_url)
    response = @http_client.put("/applications/#{application_name}", {backend_url: url_without_scheme(backend_url)})

    format_response_for response
  end

  def get_application(application_name)
    response = @http_client.get("/applications/#{application_name}")

    format_response_for response
  end

  def delete_application(application_name)
    @http_client.delete("/applications/#{application_name}")
  end

  def create_route(incoming_path, route_type, application_name)
    response = @http_client.put("/routes/#{incoming_path}", { route_type: route_type, application_id: application_name })

    format_response_for response
  end

  def create_redirect_route(incoming_path, route_type, location)
    response = @http_client.put("/routes/#{incoming_path}", { route_type: route_type, route_action: "redirect", location: location })

    format_response_for response
  end

  def get_route(incoming_path)
    response = @http_client.get("/routes/#{incoming_path}")

    format_response_for response
  end

  def delete_route(incoming_path)
    @http_client.delete("/routes/#{incoming_path}")
  end

  def format_response_for(response)
    case response.code
    when "200", "201"
      JSON.parse(response.body).symbolize_keys
    when "500"
      raise ServerError.new
    else
      raise "Unexpected HTTP response: #{response.code}: #{response.message}"
    end
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
