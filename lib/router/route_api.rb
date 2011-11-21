module Router
  class RouteApi
    attr_accessor :client
    private :client=, :client

    def initialize client
      self.client = client
    end

    def create options
      route = options.dup
      incoming_path = route.delete :incoming_path
      client.post '/routes' + incoming_path, route
    end
  end
end
