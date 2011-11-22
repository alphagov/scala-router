require 'router/errors'
require 'router/client'
require 'router/routes'
require 'router/applications'

module Router
  class API
    def initialize(base_url = "http://router.cluster")
      @client = Client.new(base_url)
    end

    def routes
      Router::Routes.new(@client)
    end

    def applications
      Router::Applications.new(@client)
    end
  end
end