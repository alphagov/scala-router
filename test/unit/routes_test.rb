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

  def encoded_body_for hash
    URI.encode_www_form hash
  end

  def assert_route_creation route
    payload = route.dup
    id = payload.delete :incoming_path
    body = encoded_body_for payload
    stub_request(:post, "http://router.cluster/routes#{id}").
      to_return(:status => 201)
    @router.routes.create route
    assert_requested :post, "http://router.cluster/routes#{id}",
      :body => body, :times => 1
  end

  def test_create_full_route
    assert_route_creation :application_id => "planner",
      :route_type => :full, :incoming_path => "/foo"
  end

  def test_create_prefix_route
    assert_route_creation :application_id => "publisher",
      :route_type => :prefix, :incoming_path => "/bar"
  end

  def test_create_duplicate_route_does_not_raise_conflict
    assert_route_creation :application_id => "publisher",
      :route_type => :full, :incoming_path => "/bar"
  end

  def test_create_overlapping_route_raises_conflict
    stub_request(:post, "http://router.cluster/routes/quux").
      to_return(:status => 409)
    assert_raise Router::Conflict do
      @router.routes.create :application_id => "jobs", :route_type => :full,
        :incoming_path => "/quux"
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

  def test_update_route
    body = encoded_body_for :application_id => "publisher",
      :route_type => :full
    stub_request(:put, "http://router.cluster/routes/bar").
      to_return(:status => 200)
    @router.routes.update :application_id => "publisher",
      :route_type => :full, :incoming_path => '/bar'
    assert_requested :put, "http://router.cluster/routes/bar",
      :body => body, :times => 1
  end

  def test_update_nonexistent_route
    body = encoded_body_for :application_id => "publisher",
      :route_type => :full
    stub_request(:put, "http://router.cluster/routes/bar").
      to_return(:status => 404)
    assert_raise Router::NotFound do
      @router.routes.update :application_id => "publisher",
        :route_type => :full, :incoming_path => '/bar'
    end
  end

  def test_get_information_for_route
    route = {
      :application_id => 'need-o-tron',
      :route_type => :full,
      :incoming_path => 'gorge'
    }
    stub_request(:get, "http://router.cluster/routes/gorge").
      to_return(:status => 200, :body => route.to_json)

    found_route = @router.routes.find '/gorge'
    assert_equal route, found_route
  end

  def test_get_information_for_nonexistant_route
    stub_request(:get, "http://router.cluster/routes/gorply").
      to_return(:status => 404)

    route = @router.routes.find '/gorply'
    assert_equal nil, route
  end
end

