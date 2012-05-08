require "test_helper"
require "router"

class ApplicationTest < MiniTest::Unit::TestCase
  include RouterTestHelper

  def setup
    trash_database
    ensure_router_running
    @router = Router.new(ROUTER_BASE_URL)
    @router.reinitialise
  end

  def test_can_create_and_update_and_delete_an_application
    response = @router.create_application("test_application", "/test/application")
    assert_equal("test_application", response[:application_id])
    assert_equal("/test/application", response[:backend_url])

    response = @router.get_application("test_application")
    assert_equal("test_application", response[:application_id])
    assert_equal("/test/application", response[:backend_url])

    response = @router.update_application("test_application", "/test/updated_application")
    assert_equal("test_application", response[:application_id])
    assert_equal("/test/updated_application", response[:backend_url])
    
    @router.delete_application("test_application")
    
    response = @router.get_application("test_application")
    assert_nil response  
  end

  def test_can_create_and_deactivate_full_routes
    response = @router.create_application("test_application", "/test/application")
    
    response = @router.create_route("test_route/test", "full", "test_application")
    assert_equal("test_application", response[:application_id])
    assert_equal("test_route/test", response[:incoming_path])
    
    response = @router.get_route("test_route/test")
    assert_equal("test_application", response[:application_id])
    assert_equal("test_route/test", response[:incoming_path])
    
    @router.delete_route("test_route/test")

    response = @router.get_route("test_route/test")
    assert_equal("gone", response[:route_action])
  end

  def test_can_create_and_delete_prefix_routes
    response = @router.create_application("test_application", "/test/application")
    
    response = @router.create_route("test_route", "prefix", "test_application")
    assert_equal("test_application", response[:application_id])
    assert_equal("test_route", response[:incoming_path])
    
    response = @router.get_route("test_route")
    assert_equal("test_application", response[:application_id])
    assert_equal("test_route", response[:incoming_path])
    
    @router.delete_route("test_route")
    
    response = @router.get_route("test_route")
    assert_nil response
  end

  def test_can_create_update_and_delete_full_redirect_routes
    response = @router.create_redirect_route("test_route/test", "full", "/example-location")
    assert_equal("test_route/test", response[:incoming_path])
    
    response = @router.get_route("test_route/test")
    assert_equal("redirect", response[:route_action])
    assert_equal("/example-location", response[:properties]['location'])
    assert_equal("test_route/test", response[:incoming_path])
    
    response = @router.create_redirect_route("test_route/test", "full", "/another-location")
    assert_equal("test_route/test", response[:incoming_path])
    assert_equal("/another-location", response[:properties]['location'])

    @router.delete_route("test_route/test")
    
    response = @router.get_route("test_route/test")
    assert_equal("gone", response[:route_action])
  end

  def test_can_create_update_and_delete_prefix_redirect_routes
    response = @router.create_redirect_route("test_route", "prefix", "/example-location")
    assert_equal("test_route", response[:incoming_path])
    
    response = @router.get_route("test_route")
    assert_equal("redirect", response[:route_action])
    assert_equal("/example-location", response[:properties]['location'])
    assert_equal("test_route", response[:incoming_path])
  
    response = @router.create_redirect_route("test_route", "prefix", "/another-location")
    assert_equal("test_route", response[:incoming_path])
    assert_equal("/another-location", response[:properties]['location'])

    @router.delete_route("test_route")
    
    response = @router.get_route("test_route")
    assert_nil response
  end

  def test_cannot_create_prefix_routes_with_more_than_one_segment
    response = @router.create_application("test_application", "/test/application")
    assert_equal("test_application", response[:application_id])

    assert_raises Router::ServerError do
      @router.create_route("test_route/test", "prefix", "test_application")
    end
  end

  def test_cannot_create_prefix_route_without_application
    assert_raises Router::ServerError do
      @router.create_route("test_route", "prefix", "test_application")
    end
  end

  def test_cannot_create_full_route_without_application
    assert_raises Router::ServerError do
      @router.create_route("test_route/test", "full", "test_application")
    end
  end


end
