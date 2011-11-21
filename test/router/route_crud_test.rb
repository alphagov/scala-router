require "test_helper"

class RouteCrudTest < Test::Unit::TestCase
  def setup
    @router = Router::Client.new :base_url => "http://router.local"
  end

  def teardown
    WebMock.reset!
  end

  def encoded_body_for hash
    URI.encode_www_form hash
  end

  def assert_route_creation route
    payload = route.dup
    id = payload.delete :incoming_path
    body = encoded_body_for payload
    stub_request(:post, "http://router.local/routes#{id}").
      to_return(:status => 201)
    @router.routes.create route
    assert_requested :post, "http://router.local/routes#{id}",
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
    stub_request(:post, "http://router.local/routes/quux").
      to_return(:status => 406)
    assert_raise Router::RouteApi::ConflictingRoute do
      @router.routes.create :application_id => "jobs", :route_type => :full,
        :incoming_path => "/quux"
    end
  end

  def test_unexpected_server_response_raises_when_creating_route
    stub_request(:post, "http://router.local/routes/BORK").
      to_return(:status => 405)
    assert_raise Router::RouteApi::UnexpectedResponse do
      @router.routes.create :application_id => "publisher",
        :route_type => :full, :incoming_path => "/BORK"
    end
  end

  def test_delete_route
    stub_request(:delete, "http://router.local/routes/foo").
      to_return(:status => 204)
    @router.routes.delete '/foo'
    assert_requested :delete, "http://router.local/routes/foo", :times => 1
  end

  def test_update_route
    body = encoded_body_for :application_id => "publisher",
      :route_type => :full
    stub_request(:put, "http://router.local/routes/bar").
      to_return(:status => 200)
    @router.routes.update :application_id => "publisher",
      :route_type => :full, :incoming_path => '/bar'
    assert_requested :put, "http://router.local/routes/bar",
      :body => body, :times => 1
  end

  def test_update_nonexistent_route
    body = encoded_body_for :application_id => "publisher",
      :route_type => :full
    stub_request(:put, "http://router.local/routes/bar").
      to_return(:status => 404)
    assert_raise Router::RouteApi::NoSuchRoute do
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
    stub_request(:get, "http://router.local/routes/gorge").
      to_return(:status => 200, :body => route.to_json)

    hash = @router.routes.find '/gorge'
    assert_equal hash, route
  end

  def test_get_information_for_nonexistant_route
    stub_request(:get, "http://router.local/routes/gorply").
      to_return(:status => 404)

    route = @router.routes.find '/gorply'
    assert_equal nil, route
  end

  def test_unexpected_server_response_raises_when_getting_route_information
    stub_request(:get, "http://router.local/routes/chicken").
      to_return(:status => 418)
    assert_raise Router::RouteApi::UnexpectedResponse do
      @router.routes.find '/chicken'
    end
  end
end

