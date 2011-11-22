module Router
  class RouterError < StandardError; end

  class RemoteError < RouterError
    attr_accessor :response
    private :response=

    def initialize(message, response)
      super(message)
      self.response = response
    end
  end

  class Conflict < RemoteError; end
  class NotFound < RemoteError; end
end
