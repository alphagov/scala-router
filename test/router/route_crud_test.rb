require "test_helper"

class RouteCrudTest < Test::Unit::TestCase
  def setup
    @router = Router::Client.new "http://router.cluster"
  end

# A route looks like this:
#     {
#       "applicationId": "publisher",
#       "routeType": "prefix|full",
#       "incomingPath": "/foo"
#     }

  def post_body_for hash
    hash.map do |key, value|
      encoded_key = CGI.escape key.to_s
      encoded_value = CGI.escape value.to_s
      "#{encoded_key}=#{encoded_value}"
    end.join '&'
  end

  def assert_route_creation route
    payload = route.dup
    id = payload.delete :incoming_path
    body = post_body_for payload
    stub_request(:post, "http://router.cluster/routes#{id}").
      to_return(:status => 200)
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
    assert_route_creation :application_id => "publisher",
      :route_type => :prefix, :incoming_path => "/bar/*"
    assert_route_creation :application_id => "publisher",
      :route_type => :full, :incoming_path => "/bar/quuz"
  end

  def test_delete_route
    
  end

  def test_delete_nonexistent_route
  end

  def test_update_route
  end

  def test_update_nonexistent_route
  end

  def test_get_information_for_route
  end

  def test_get_information_for_nonexistant_route
  end

  def stub_post path, options
    stub_request(:post, "http://router.cluster/routes").
      with(:headers => {'Accept'=>'*/*', 'User-Agent'=>'Ruby'}).
      to_return(:status => 200, :body => "", :headers => {})
  end

end

