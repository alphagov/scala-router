require 'router/restful_resource_collection'

module Router
  class Applications < RestfulResourceCollection

    def collection_url(application_id)
      '/applications/' + application_id
    end
    def id_attribute; :application_id; end
    
  end
end
