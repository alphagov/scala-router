require "test_helper"
require "router"

class HttpClientTest < MiniTest::Unit::TestCase
  include RouterTestHelper

  def setup
    trash_database
    ensure_router_running

    @http_client = Router::HttpClient.new(ROUTER_BASE_URL)
    @http_client.get("/reinitialise") # yup this is hacky
  end

  def test_returns_correct_responses_for_application_manipulations
    application_name = 'test_application'
    backend_url      = '/test/application'

    response = @http_client.put("/applications/#{application_name}", {backend_url: backend_url})
    assert_equal( "201", response.code )

    response_body = JSON.parse(response.body)
    assert_equal("test_application", response_body["application_id"])
    assert_equal("/test/application", response_body["backend_url"])

    response = @http_client.put("/applications/#{application_name}", {backend_url: "/test/updated_application"})
    assert_equal( "200", response.code )

    response_body = JSON.parse(response.body)
    assert_equal("test_application", response_body["application_id"])
    assert_equal("/test/updated_application", response_body["backend_url"])
    
    response = @http_client.delete("/applications/test_application")
    assert_equal("204", response.code)

    response = @http_client.get("/applications/test_application")
    assert_equal("404", response.code) 
  end

  def test_returns_correct_responses_for_full_route_manipulations
    response = @http_client.put("/applications/test_application", {backend_url: "/test/application"})
    assert_equal("201", response.code)

    response = @http_client.put("/routes/test_route/test", {route_type: "full", application_id: "test_application"})
    assert_equal("201", response.code)

    response_body = JSON.parse(response.body)
    assert_equal("test_application", response_body["application_id"])
    assert_equal("test_route/test", response_body["incoming_path"])
    
    response = @http_client.get("/routes/test_route/test")
    assert_equal("200", response.code)
    
    response_body = JSON.parse(response.body)
    assert_equal("test_application", response_body["application_id"])
    assert_equal("test_route/test", response_body["incoming_path"])
    
    response = @http_client.delete("/routes/test_route/test")
    assert_equal("200", response.code)
    
    response_body = JSON.parse(response.body)
    assert_equal("gone", response_body["route_action"])

    response = @http_client.get("/routes/test_route/test")
    assert_equal("200", response.code)
    
    response_body = JSON.parse(response.body)
    assert_equal("gone", response_body["route_action"])
  end

  def test_returns_correct_response_for_prefix_route_manipulations
    response = @http_client.put("/applications/test_application", { backend_url: "/test/application" })
    assert_equal("201", response.code)

    response = @http_client.put("/routes/test_route", route_type: "prefix", application_id: "test_application")
    assert_equal("201", response.code)

    response_body = JSON.parse(response.body)
    assert_equal("test_application", response_body["application_id"])
    assert_equal("test_route", response_body["incoming_path"])
    
    response = @http_client.get("/routes/test_route")
    assert_equal("200", response.code)
    
    response_body = JSON.parse(response.body)
    assert_equal("test_application", response_body["application_id"])
    assert_equal("test_route", response_body["incoming_path"])
    
    response = @http_client.delete("/routes/test_route")
    assert_equal("204", response.code)
 
    response = @http_client.get("/routes/test_route")
    assert_equal("404", response.code)
  end

  def test_returns_error_response_when_creating_prefix_route_with_more_than_one_segment
    response = @http_client.put("/applications/test_application", { backend_url: "/test/application" })
    assert_equal("201", response.code)

    response = @http_client.put("/routes/test_route/test", { route_type: "prefix", application_id: "test_application" })
    assert_equal("500", response.code)
  end

  def test_returns_error_response_when_creating_prefix_route_without_application  
    response = @http_client.put("/routes/test_route", { route_type: "prefix", application_id: "test_application_test1" })
    assert_equal("500", response.code)
  end

  def test_returns_error_response_when_creating_full_route_without_application
    response = @http_client.put("/routes/test_route/test", { route_type: "full", application_id: "test_application_test2" })
    assert_equal("500", response.code)
  end

end