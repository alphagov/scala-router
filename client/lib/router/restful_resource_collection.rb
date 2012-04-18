class Router
  class RestfulResourceCollection
    attr_accessor :http_client
    private :http_client=, :http_client

    attr_accessor :logger
    private :logger=, :logger

    def collection_url(id); raise "Not implemented"; end
    def id_attribute; raise "Not implemented"; end

    def initialize http_client, logger = nil
      self.http_client = http_client
      self.logger = logger || NullLogger.instance
    end

    def create options
      logger.debug "create with options = #{options.inspect}"
      http_client.post collection_url(options[id_attribute]), without_id(options)
    end

    def update options
      logger.debug "update with options = #{options.inspect}"
      http_client.put collection_url(options[id_attribute]), without_id(options)
    end

    def delete id
      logger.debug "delete with id = #{id}"
      http_client.delete collection_url(id)
    end

    def find id
      logger.debug "find with id = #{id}"
      http_client.get collection_url(id)
    rescue NotFound
      logger.debug "couldn't find with id = #{id}"
      nil
    end

    protected
    def without_id options
      options.reject { |k,v| k == id_attribute }
    end
  end
end
