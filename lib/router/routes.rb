require 'router/restful_resource_collection'

module Router
  class Routes < RestfulResourceCollection
    
    def collection_url(incoming_path); 
      '/routes' + incoming_path
    end
    
    def id_attribute
      :incoming_path
    end
    
    def create(*args); symbolize_route_type super; end
    def update(*args); symbolize_route_type super; end
    def find(*args); symbolize_route_type super; end
    
    protected
      def symbolize_route_type(route)
        route.merge(route_type: route[:route_type].to_sym) if route
      end
  end
end
