require 'router/restful_resource_collection'

module Router
  class Routes < RestfulResourceCollection
    def collection_url(incoming_path)
      '/routes' + incoming_path
    end

    def id_attribute
      :incoming_path
    end
  end
end
