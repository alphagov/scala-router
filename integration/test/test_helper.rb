require "bundler"
Bundler.require(:default)
require "minitest/autorun"
require "mongo"
require "open-uri"

Thread.abort_on_exception = true

module RouterTestHelper
  ROUTER_PORT = 11200
  ROUTER_BASE_URL = "http://localhost:#{ROUTER_PORT}"
  TEST_SERVER_PORT = ROUTER_PORT + 1
  TEST_SERVER_BASE_URL = "http://localhost:#{TEST_SERVER_PORT}"

  def trash_database
    conn = Mongo::Connection.new
    conn.drop_database "router-dev"
  end

  def ensure_router_running
    return if $router_io # Use global to prevent garbage collection
    Dir.chdir(File.expand_path("../../../router", __FILE__)) do
      $router_io = IO.popen("./start-router-locally.sh", "r+")
      while (line = $router_io.gets.chomp) !~ /success/
        $stderr.puts line unless line.empty?
        raise "Starting router failed" if line =~ /error/
      end
      $stderr.puts line
    end
  end

  def ensure_test_server_running
    return if $test_server # Use global to prevent garbage collection
    $test_server = WEBrick::HTTPServer.new(:Port => TEST_SERVER_PORT)

    Thread.new do
      $test_server.start
    end
    sleep 0.2
  end

  def create_test_responder(route)
    $test_server.mount_proc route do |req, res|
      res.body = route
    end
  end

  def get(url)
    open(ROUTER_BASE_URL + url) do |f|
      return f.read
    end
  end
end
