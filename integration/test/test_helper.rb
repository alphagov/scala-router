require "bundler"
Bundler.require(:default)
require "minitest/autorun"

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
        line.empty? ? sleep(0.1) : $stderr.puts(line)
        raise "Starting router failed" if line =~ /error/
      end
      $stderr.puts line

      Thread.new do
        while (line = $router_io.gets.chomp) !~ /exit/
          line.empty? ? sleep(0.1) : $stderr.puts(line)        
        end
      end.run
    end
  end

end
