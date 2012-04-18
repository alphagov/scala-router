class Router
  class ResponseParser
    def self.parse(response)
      return unless is_json?(response)
      filter(Hash[
        JSON.parse(response.body).map {|k,v| [k.to_sym, v]}
      ])
    end
    
    def self.is_json?(response)
      response.body and
        response.body.size > 0 and
        response.body[0] == '{'
    end
    
    def self.filter(route)
      ensure_incoming_path_has_forward_slash_prefix(symbolize_route_type(route))
    end
    
    def self.ensure_incoming_path_has_forward_slash_prefix(route)
      if route[:incoming_path] && route[:incoming_path][0] != '/'
        route[:incoming_path] = '/' + route[:incoming_path]
      end
      route
    end
    
    def self.symbolize_route_type(route)
      if route[:route_type]
        route[:route_type] = route[:route_type].to_sym
      end
      route
    end
  end
end