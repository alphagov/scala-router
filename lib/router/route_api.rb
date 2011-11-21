module Router
  class RouteApi
    class RemoteError < RuntimeError; end
    class ConflictingRoute < RemoteError; end
    class NoSuchRoute < RemoteError; end

    attr_accessor :client
    private :client=, :client

    def initialize client
      self.client = client
    end

    def create options
      route = options.dup
      incoming_path = route.delete :incoming_path
      response = client.post '/routes' + incoming_path, route
      raise ConflictingRoute if response.code.to_i == 406
    end

    def delete path
      client.delete '/routes' + path
    end

    def update options
      route = options.dup
      incoming_path = route.delete :incoming_path
      response = client.put '/routes' + incoming_path, route
      raise NoSuchRoute if response.code.to_i == 404
    end
  end
end
