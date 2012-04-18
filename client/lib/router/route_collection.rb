require 'router/restful_resource_collection'

class Router
  class RouteCollection < RestfulResourceCollection
    def collection_url(incoming_path)
      '/routes' + incoming_path
    end

    def id_attribute
      :incoming_path
    end
  end
end
