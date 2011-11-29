require "test_helper"

class RoutesTest < Test::Unit::TestCase
  def setup
    http_client = Router::HttpClient.new("http://router.cluster")
    @router = Router::Client.new(http_client)
  end

  def teardown
    WebMock.reset!
  end

# A route looks like this:
#     {
#       "applicationId": "publisher",
#       "routeType": "prefix|full",
#       "incomingPath": "/foo"
#     }


  def post_create_route route
    stub_request(:post, "http://router.cluster/routes#{route.incoming_path}").
      to_return(:status => 201)
    @router.routes.create route.to_hash
  end
  
  class RouteFixture
    def initialize(route)
      @route = route
    end
    
    def incoming_path
      @route[:incoming_path]
    end
    
    def without(*fields)
      Hash[@route.reject do |k,v| 
        fields.include?(k)
      end]
    end
    
    def encoded_body
      URI.encode_www_form without(:incoming_path)
    end
    
    def to_hash
      @route.dup
    end
    
    def to_json
      to_hash.to_json
    end
  end
  
  def test_create_full_route
    route = RouteFixture.new({
      :application_id => "planner",
      :route_type => :full, 
      :incoming_path => "/foo"
    })
    post_create_route route
    assert_requested :post, "http://router.cluster/routes#{route.incoming_path}",
      :body => route.encoded_body, :times => 1
  end

  def test_create_prefix_route
    route = RouteFixture.new(
      :application_id => "publisher",
      :route_type => :prefix, 
      :incoming_path => "/bar"
    )
    post_create_route(route)
    assert_requested :post, "http://router.cluster/routes#{route.incoming_path}",
       :body => route.encoded_body, :times => 1
  end

  def test_exception_raised_when_router_reports_conflict_on_creation
    existing = RouteFixture.new(application_id: "publisher", route_type: :prefix, incoming_path: "/qu")
    stub_request(:post, "http://router.cluster/routes/quux").to_return(status: 409, body: existing.to_json)
    begin
      @router.routes.create :application_id => "jobs", :route_type => :full,
        :incoming_path => "/quux"
      fail "Expected Router::Conflict exception"
    rescue Router::Conflict => e
      assert_equal existing.to_hash, e.existing
    end
  end

  def test_other_400_error_raises_remote_error
    stub_request(:post, "http://router.cluster/routes/quux").
      to_return(:status => 456)
    begin
      @router.routes.create :application_id => "jobs", :route_type => :full,
        :incoming_path => "/quux"
      fail "Expected exception, but none raised"
    rescue Router::RemoteError => e
      assert_equal 456, e.response.code.to_i
    end
  end

  def test_other_500_error_raises_remote_error
    stub_request(:post, "http://router.cluster/routes/quux").
      to_return(:status => 500)
    begin
      @router.routes.create :application_id => "jobs", :route_type => :full,
        :incoming_path => "/quux"
      fail "Expected exception, but none raised"
    rescue Router::RemoteError => e
      assert_equal 500, e.response.code.to_i
    end
  end

  def test_delete_route
    stub_request(:delete, "http://router.cluster/routes/foo").
      to_return(:status => 204)
    @router.routes.delete '/foo'
    assert_requested :delete, "http://router.cluster/routes/foo", :times => 1
  end

  def test_update_route_sends_http_put
    route = RouteFixture.new(
      :application_id => "publisher",
      :route_type => :full,
      :incoming_path => '/bar'
    )
    stub_request(:put, "http://router.cluster/routes#{route.incoming_path}").
      to_return(:status => 200)
    @router.routes.update route.to_hash
    assert_requested :put, "http://router.cluster/routes#{route.incoming_path}",
      :body => route.encoded_body, :times => 1
  end

  def test_404_on_update_raises_error
    route = RouteFixture.new(
      :application_id => "publisher",
      :route_type => :full,
      :incoming_path => '/bar'
    )
    stub_request(:put, "http://router.cluster/routes#{route.incoming_path}").
      to_return(:status => 404)
    assert_raise Router::NotFound do
      @router.routes.update route.to_hash
    end
  end

  def test_finding_a_route_sends_http_get_and_parses_json_response
    route = RouteFixture.new(
      :application_id => 'need-o-tron',
      :route_type => :full,
      :incoming_path => '/gorge'
    )
    stub_request(:get, "http://router.cluster/routes#{route.incoming_path}").
      to_return(:status => 200, :body => route.to_json)

    found_route = @router.routes.find route.incoming_path
    assert_equal route.to_hash, found_route
  end

  def test_get_information_for_nonexistant_route_returns_nil
    stub_request(:get, "http://router.cluster/routes/gorply").
      to_return(:status => 404)

    route = @router.routes.find '/gorply'
    assert_equal nil, route
  end
  
  def test_when_getting_route_client_always_prefixes_incoming_path_with_forward_slash
    router_response_body = {
      :application_id => 'need-o-tron',
      :route_type => :full,
      :incoming_path => 'gorge'
    }.to_json
    
    stub_request(:get, "http://router.cluster/routes/gorge").
      to_return(:status => 200, :body => router_response_body)
    
    actual_route = @router.routes.find '/gorge'
    
    assert_equal '/gorge', actual_route[:incoming_path]
  end
end

