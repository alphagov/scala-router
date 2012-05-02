require "test_helper"
require "router"

class ApplicationTest < MiniTest::Unit::TestCase
  include RouterTestHelper

  def setup
    trash_database
    ensure_router_running
    @router = Router.new(ROUTER_BASE_URL)
  end

  def test_can_create_and_update_and_delete_an_application
    response = @router.create_application("test_application", "/test/application")
    assert_equal("201", response.code)

    response = @router.get_application("test_application")
    assert_equal("200", response.code)

    response_body = JSON.parse(response.body)
    assert_equal("test_application", response_body["application_id"])
    assert_equal("/test/application", response_body["backend_url"])

    response = @router.update_application("test_application", "/test/updated_application")
    assert_equal("200", response.code)

    response_body = JSON.parse(response.body)
    assert_equal("test_application", response_body["application_id"])
    assert_equal("/test/updated_application", response_body["backend_url"])
    
    response = @router.delete_application("test_application")
    assert_equal("204", response.code)

    response = @router.get_application("test_application")
    assert_equal("404", response.code) 
  end

  def test_can_create_and_deactivate_full_routes
    response = @router.create_application("test_application", "/test/application")
    assert_equal("201", response.code)

    response = @router.create_route("test_route/test", "full", "test_application")
    assert_equal("201", response.code)

    response_body = JSON.parse(response.body)
    assert_equal("test_application", response_body["application_id"])
    assert_equal("test_route/test", response_body["incoming_path"])
    
    response = @router.get_route("test_route/test")
    assert_equal("200", response.code)
    
    response_body = JSON.parse(response.body)
    assert_equal("test_application", response_body["application_id"])
    assert_equal("test_route/test", response_body["incoming_path"])
    
    # response = @router.delete_route("test_route/test")
    # assert_equal("200", response.code)
    
    # response_body = JSON.parse(response.body)
    # assert_equal("gone", response_body["route_action"])

    # response = @router.get_route("test_route/test")
    # assert_equal("200", response.code)
    
    # response_body = JSON.parse(response.body)
    # assert_equal("gone", response_body["route_action"])
  end

  def test_can_create_and_delete_prefix_routes
    response = @router.create_application("test_application", "/test/application")
    assert_equal("201", response.code)

    # come back to this - why does it make line 57 fail? the database should be trashed!
    # it's something to do with it not creating the gone application I think
    response = @router.create_route("test_route", "prefix", "test_application")
    assert_equal("201", response.code)

    response_body = JSON.parse(response.body)
    assert_equal("test_application", response_body["application_id"])
    assert_equal("test_route", response_body["incoming_path"])
    
    response = @router.get_route("test_route")
    assert_equal("200", response.code)
    
    response_body = JSON.parse(response.body)
    assert_equal("test_application", response_body["application_id"])
    assert_equal("test_route", response_body["incoming_path"])
    
    response = @router.delete_route("test_route")
    assert_equal("204", response.code)
 
    response = @router.get_route("test_route")
    assert_equal("404", response.code)
  end

 def test_cannot_create_prefix_routes_with_more_than_one_segment
    response = @router.create_application("test_application", "/test/application")
    assert_equal("201", response.code)

    response = @router.create_route("test_route/test", "prefix", "test_application")
    assert_equal("500", response.code)
 end

  def test_cannot_create_prefix_route_without_application
    response = @router.create_route("test_route", "prefix", "test_application")
    assert_equal("500", response.code)
  end

  def test_cannot_create_full_route_without_application
    response = @router.create_route("test_route/test", "full", "test_application")
    assert_equal("500", response.code)
  end

end
