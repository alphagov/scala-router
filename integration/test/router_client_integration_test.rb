require "test_helper"
require "router"
require "webrick"

class ApplicationTest < MiniTest::Unit::TestCase
  include RouterTestHelper

  def setup
    trash_database
    ensure_router_running
    ensure_test_server_running
    @router = Router.new(ROUTER_BASE_URL)
  end

  def test_should_register_prefix_route
    create_test_responder "/prefix/suffix"
    @router.application("test", TEST_SERVER_BASE_URL) do |app|
      app.ensure_prefix_route "/prefix"
    end
    assert_equal "/prefix/suffix", get("/route/prefix/suffix")
  end

  def test_should_register_full_route
    create_test_responder "/full"
    @router.application("test", TEST_SERVER_BASE_URL) do |app|
      app.ensure_full_route "/full"
    end
    assert_equal "/full", get("/route/full")
  end
end
