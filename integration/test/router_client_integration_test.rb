require "test_helper"
require "router"
require "webrick"

class ApplicationTest < MiniTest::Unit::TestCase
  include RouterTestHelper

  def setup
    trash_database
    ensure_router_running
   # ensure_test_server_running #we don't need that 
    @router = Router.new(ROUTER_BASE_URL)
  end

  # def test_should_register_prefix_route
  #   create_test_responder "/prefix/suffix"
  #   @router.application("test", TEST_SERVER_BASE_URL) do |app|
  #     app.ensure_prefix_route "/prefix"
  #   end
  #   assert_equal "/prefix/suffix", get("/route/prefix/suffix")
  # end

  # def test_should_register_full_route
  #   create_test_responder "/full"
  #   @router.application("test", TEST_SERVER_BASE_URL) do |app|
  #     app.ensure_full_route "/full"
  #   end
  #   assert_equal "/full", get("/route/full")
  # end

  def test_can_create_and_update_and_delete_an_application
    response = @router.create_or_update_application("test_application", "/test/application")
    assert_equal("201", response.code)

    # response = @router.get_application ???

    response = @router.create_or_update_application("test_application", "/test/updated_application")
    assert_equal("200", response.code)
    
    response = @router.delete_application("test_application")
    assert_equal("204", response.code)
  end

  def test_can_create_update_and_delete_full_routes
    response = @router.create_or_update_application("test_application", "/test/application")
    assert_equal("201", response.code)

    response = @router.create_route("test_route/test", "full", "test_application")
    assert_equal("201", response.code)

    response = @router.get_route("test_route/test")
    assert_equal("200", response.code)
    
    response_body = JSON.parse(response.body)
    assert_equal("test_application", response_body["application_id"])

    # name for method - we update route with new application
    # response = @router.create_or_update_route("test_route/updated_route", "full", "test_application") 
    # assert_equal("201", response.code)

    # response = @router.get_route("test_route/test")
    # assert_equal("20", response.code)

  end

  def test_can_create_update_and_delete_prefix_routes
    response = @router.create_or_update_application("test_application", "/test/application")
    assert_equal("201", response.code)

    response = @router.create_route("test_route", "prefix", "test_application")
    assert_equal("201", response.code)

    response = @router.get_route("test_route")
    assert_equal("200", response.code)
    
    response_body = JSON.parse(response.body)
    assert_equal("test_application", response_body["application_id"])


  end



  # def test_cannot_create_prefix_routes_with_more_than_one_segment
  #   response = @router.create_or_update_application("test_application", "/test/application")
  #   assert_equal("201", response.code)

  #   #this throws a RuntimeException - can I test for this?  
  #   response = @router.create_or_update_route("test_route/test", "prefix", "test_application")
  # end

  # def test_cannot_create_full_route_without_application
  #   response = @router.create_or_update_route("test_route", "prefix", "test_application")
    
  #   #this throws a JavaLang exception
  #   assert_equal("201", response.code)
  # end

  # def test_cannot_create_prefix_route_without_application


  # end


  # def test_can_create_new_full_route
  #   new_route = "/routes/new-route-created"
    

  #   # # assert_raises(OpenUri::HTTPError: 404 Not Found)
  #   # #   get(new_route)
  #   # # end

  #   #this is using the test helper get so how can I get a 404 and test for it?

  #   create_test_responder(new_route)

  #   @router.route do |route|
  #     route.create_or_update_route(new_route, "application", "full")
  #   end

  #   assert_equal(new_route, get(new_route))
    
  # end  
  
end
