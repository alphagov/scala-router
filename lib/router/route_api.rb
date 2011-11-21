module Router
  class RouteApi
    class RemoteError < RuntimeError; end
    class ConflictingRoute < RemoteError; end
    class NoSuchRoute < RemoteError; end
    class UnexpectedResponse < RemoteError; end

    attr_accessor :client
    private :client=, :client

    attr_accessor :logger
    private :logger=, :logger

    def initialize client, logger
      self.client = client
      self.logger = logger
    end

    def create options
      logger.debug "Creating route: #{options.inspect}"
      route = options.dup
      incoming_path = route.delete :incoming_path
      logger.debug "Route is for path #{incoming_path}"
      response = client.post '/routes' + incoming_path, route
      logger.debug "Router responded with status #{response.code}"
      raise ConflictingRoute if response.code.to_i == 406
      raise UnexpectedResponse unless response.code.to_i == 201
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

    def find path
      logger.debug "Finding route #{path}"
      response = client.get '/routes' + path
      logger.debug "Router responded with status #{response.code}"
      return if response.code.to_i == 404
      raise UnexpectedResponse unless response.code.to_i == 200
      logger.debug "Parsing response as JSON"
      route = JSON.parse response.body
      logger.debug "Response parsed to #{route.inspect}"
      # FIXME: With a lot of invalid route_type values I could probably craft a
      # way to fill up local memory with unused symbols.
      found = {
        :application_id => route['application_id'],
        :route_type     => route['route_type'].to_sym,
        :incoming_path  => route['incoming_path']
      }
      logger.debug "Returning #{found.inspect}"
      found
    end
  end
end
