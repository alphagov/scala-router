module Router
  class HttpClient
    attr_accessor :base_url
    private :base_url=, :base_url

    attr_accessor :logger
    private :logger=, :logger

    def initialize(base_url, logger = nil)
      self.base_url = base_url
      self.logger = logger || NullLogger.instance
    end

    def router_url(partial_uri)
      URI.parse(base_url + partial_uri)
    end
    private :router_url

    def post(partial_uri, params)
      do_request Net::HTTP::Post, partial_uri, params
    end

    def put(partial_uri, params)
      do_request Net::HTTP::Put, partial_uri, params
    end

    def delete(partial_uri)
      do_request Net::HTTP::Delete, partial_uri
    end

    def get(partial_uri)
      do_request Net::HTTP::Get, partial_uri
    end

    protected
    def do_request(verb, partial_uri, form_data = nil)
      uri = router_url(partial_uri)
      request = verb.new(uri.path)
      request.form_data = form_data if form_data
      logger.debug "#{verb::METHOD}: #{uri} #{form_data.inspect}"
      response = Net::HTTP.new(uri.host, uri.port).start do |http|
        http.request(request)
      end
      logger.debug "Router responded with status: #{response.code}"
      raise_on_error(response)
      parse_json(response.body) if is_json?(response)
    end

    def is_json?(response)
      response.body and
        response.body.size > 0 and
        response.body[0] == '{'
    end

    def parse_json(raw_json)
      logger.debug "Parsing response as JSON"
      parsed = Hash[
        JSON.parse(raw_json).map {|k,v| [k.to_sym, v]}
      ]
      logger.debug "Response parsed to #{parsed.inspect}"
      parsed
    end

    def raise_on_error(response)
      case response.code.to_i
      when 409 then raise Conflict.new("Conflict", response)
      when 404 then raise NotFound.new("Not found", response)
      when 400..599 then
        raise RemoteError.new("Remote error", response)
      else
        response
      end
    end
  end
end
