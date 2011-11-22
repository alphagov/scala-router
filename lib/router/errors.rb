module Router
  class RouterError < StandardError; end
  
  class RemoteError < RouterError
    attr_reader :response

    def initialize(message, response)
      super(message)
      @response = response
    end
  end

  class Conflict < RemoteError; end
  class NotFound < RemoteError; end
end