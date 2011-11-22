require 'net/http'
require 'ostruct'
require 'json'
require 'router/route_api'

module Router
  class Conflict < RuntimeError
  end

  class Client
    def initialize(base_url = "http://router.cluster")
      @base_url = base_url
    end

    def routes
      Router::RouteApi.new self
    end

    def create_application(application_id, backend_url)
      response post "/applications/#{application_id}", {'backend_url' => backend_url}
    end

    def get_application(application_id)
      response get "/applications/#{application_id}"
    end

    def update_application(application_id, params)
      response put "/applications/#{application_id}", params
    end

    def delete_application(application_id)
      delete "/applications/#{application_id}"
    end

    def router_url(uri)
      URI.parse(@base_url + uri)
    end
    private :router_url

    def post(uri, params)
      Net::HTTP.post_form(router_url(uri), params)
    end

    def put(uri_str, params)
      uri = router_url(uri_str)
      put_request = Net::HTTP::Put.new(uri_str)
      put_request.form_data = params
      Net::HTTP.new(uri.host, uri.port).start { |http| http.request(put_request) }
    end

    def delete(uri_str)
      uri = router_url(uri_str)
      delete_request = Net::HTTP::Delete.new(uri_str)
      Net::HTTP.new(uri.host, uri.port).start { |http| http.request(delete_request) }
    end

    def get(uri_str)
      Net::HTTP.get_response(router_url uri_str)
    end

    def response(response)
      raise Conflict if response.kind_of? Net::HTTPConflict
      to_ostruct JSON.parse(response.body)
    end

    def to_ostruct(obj)
      case obj
      when Hash
        values = {}
        obj.each { |key, value| values[key] = to_ostruct(value) }
        OpenStruct.new(values)
      else
        obj
      end
    end
    private :to_ostruct
  end
end
