class Router
  class RouterError < StandardError; end

  class RemoteError < RouterError
    attr_accessor :response
    private :response=

    def initialize(message, response)
      super(message)
      self.response = response
    end
  end

  class Conflict < RemoteError
    def existing
      ResponseParser.parse(response)
    end
  end
  
  class NotFound < RemoteError; end
end
