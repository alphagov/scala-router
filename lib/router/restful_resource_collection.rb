module Router
  class RestfulResourceCollection
    
    def collection_url(id); raise "Not implemented"; end
    def id_attribute; raise "Not implemented"; end
    
    def initialize(http_client)
      @http_client = http_client
    end
    
    def create options
      @http_client.post collection_url(options[id_attribute]), without_id(options)
    end

    def update options
      @http_client.put collection_url(options[id_attribute]), without_id(options)
    end
    
    def delete id
      @http_client.delete collection_url(id)
    end

    def find id
      @http_client.get collection_url(id)
    rescue NotFound
      nil
    end

    protected
      def without_id options
        options.reject { |k,v| k == id_attribute }
      end
    end
end
