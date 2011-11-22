require 'net/http'
require 'ostruct'
require 'json'

module Router

  class Client
    def initialize(base_url)
      @base_url = base_url
    end
    
    def router_url(uri)
      URI.parse(@base_url + uri)
    end
    private :router_url

    def post(uri, params)
      parse Net::HTTP.post_form(router_url(uri), params)
    end

    def put(uri_str, params)
      uri = router_url(uri_str)
      put_request = Net::HTTP::Put.new(uri_str)
      put_request.form_data = params
      parse Net::HTTP.new(uri.host, uri.port).start { |http| http.request(put_request) }
    end

    def delete(uri_str)
      uri = router_url(uri_str)
      delete_request = Net::HTTP::Delete.new(uri_str)
      parse Net::HTTP.new(uri.host, uri.port).start { |http| http.request(delete_request) }
    end

    def get(uri_str)
      parse Net::HTTP.get_response(router_url uri_str)
    end
    
  protected
    def parse(response)
      raise_on_error(response)
      parse_json(response.body) if is_json?(response)
    end
    
    def is_json?(response)
      response.body and
        response.body.size > 0 and
        response.body[0] == '{'
    end
    
    def parse_json(raw_json)
      Hash[
        JSON.parse(raw_json).map {|k,v| [k.to_sym, v]}
      ]
    end
    
    def raise_on_error(response)
      case response.code.to_i
      when 409 then raise Conflict.new("Conflict", response)
      when 404 then raise NotFound.new("Not found", response)
      when 400..499 then 
        raise RemoteError.new("Remote error", response)
      else
        response
      end
    end
  end
end
