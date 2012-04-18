require "test_helper"

class RouterTest < Test::Unit::TestCase
  def setup
    router_stub = stub_everything('router_client', 
      applications: stub_everything('apps'),
      routes: stub_everything('routes')
    )
    Router::Client.stubs(:new).returns(router_stub)
  end

  def test_creating_an_application_yields_an_application
    yielded = false
    Router.new("http://router.url").application("id", "backend.host") do |app|
      yielded = app
    end
    assert_kind_of Router::Application, yielded, "Should have yielded on application creation"
  end

  def test_creating_an_application_returns_the_application
    app = Router.new("http://router.url").application("id", "backend.host")
    assert_kind_of Router::Application, app, "Should have returned an application"
  end

  def test_application_is_created_with_a_configured_router_client
    router_client = stub(:router_client)
    Router::Client.expects(:new).with("http://router.url", anything).returns(router_client)
    Router::Application.expects(:new).with(router_client, anything, anything).returns(stub_everything)
    
    Router.new("http://router.url").application("id", "backend.host")
  end

  def test_logger_passed_to_router_client_if_provided
    logger = stub('logger')
    Router::Client.expects(:new).with("http://router.url", logger)
    
    Router.new("http://router.url", logger)
  end
  
  def test_application_is_created_with_application_id_and_backed_url
    Router::Client.expects(:new).with("http://router.url", anything).returns(stub(:router_client))
    Router::Application.expects(:new).with(anything, "id", "backend.host").returns(stub_everything)
    
    Router.new("http://router.url").application("id", "backend.host")
  end

  def test_should_register_application_with_router
    router = Router.new("http://router.url")
    application_stub = stub(:application)
    Router::Application.stubs(:new).returns(application_stub)
    application_stub.expects(:register!)

    router.application("id", "backend.host") do |app|
    end
  end
end