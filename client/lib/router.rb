require 'router/client'
require 'uri'

class Router

  def initialize(router_endpoint_url, logger=nil)
    @http_client = Router::HttpClient.new(router_endpoint_url)
  end

  def create_or_update_application(application_name, backend_url)
    response = @http_client.put("/applications/#{application_name}", {backend_url: url_without_scheme(backend_url)})
  end

  def delete_application(application_name)
    response = @http_client.delete("/applications/#{application_name}")
  end 

  def create_route(route, route_type, application_name)
    response = @http_client.put("/routes/#{route}", { route_type: route_type, application_id: application_name })
  end

  def get_route(route)
    response = @http_client.get("/routes/#{route}")
  end

  #Here - method for updating route, with a different application... see scala test
  #router_client.create_or_update_route :from "/foo"/bar" :to "publisher" :type "full"  


  # def create_or_update_route(existing_route, new_route, route_type) 

  #  # :from "/foo.bar" :to "http://news.bbc.co.uk" :type: redirect

  # end

  def delete_route(route)
    #delete_route "/foo/bar"
  end


  def move_route() 
    #:from "/foo/bar" :to "/foo/bang" :application (if diff) :smart_answers 
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