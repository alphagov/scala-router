require 'router/restful_resource_collection'

module Router
  class Routes < RestfulResourceCollection
    def collection_url(incoming_path)
      '/routes' + incoming_path
    end

    def id_attribute
      :incoming_path
    end

    def create(*args); filter super; end
    def update(*args); filter super; end
    def find(*args); filter super; end
    
    protected
      def filter(route)
        ensure_incoming_path_has_forward_slash_prefix(symbolize_route_type(route))
      end
      
      def ensure_incoming_path_has_forward_slash_prefix(route)
        if route && route[:incoming_path] && route[:incoming_path][0] != '/'
          route[:incoming_path] = '/' + route[:incoming_path]
        end
        route
      end
      
      def symbolize_route_type(route)
        route.merge(route_type: route[:route_type].to_sym) if route
      end
  end
end
