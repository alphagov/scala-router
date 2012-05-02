require 'net/http'
require 'null_logger'
require 'json'

class Router
  class HttpClient
   
    def initialize(base_url, logger = nil)
      @base_url = base_url
      @logger = logger || NullLogger.instance
    end

    def router_url(partial_uri)
      URI.parse(@base_url + partial_uri)
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
      @logger.debug "#{verb::METHOD}: #{uri} #{form_data.inspect}"
      
      #why is this in a block?
      response = Net::HTTP.new(uri.host, uri.port).start do |http|
        http.request(request)
      end
            @logger.debug "Router responded with status: #{response.code}"
      response
    #I don't think we need this 
    #Router::ResponseParser.parse(response)
    end

  end
end
