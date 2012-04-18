require "test_helper"

class ApplicationCollectionTest < Test::Unit::TestCase

  def setup
    @router_api = Router::Client.new("http://router.cluster")
    @params = {
      application_id: "test-application",
      backend_url: "http://jobs.alphagov.co.uk"
    }
  end

  def test_create_sends_http_post_to_router
    stub_request(:post, "http://router.cluster/applications/#{@params[:application_id]}").
        with(:body => {"backend_url"=>@params[:backend_url]}).
        to_return(:status => 201, :body => @params.to_json)
    application = @router_api.applications.create(@params)
    assert_equal @params[:application_id], application[:application_id]
    assert_equal @params[:backend_url], application[:backend_url]
  end

  def test_router_reported_conflict_on_create_raises_conflict_error
    stub_request(:post, "http://router.cluster/applications/#{@params[:application_id]}").
        to_return(:status => 409, :body => @params.to_json)
    begin
      @router_api.applications.create @params
    rescue Router::Conflict => e
      assert_equal @params, e.existing
    end
  end

  def test_find_sends_http_get
    stub_request(:get, "http://router.cluster/applications/#{@params[:application_id]}").
        to_return(:status => 200, :body => @params.to_json)
    application = @router_api.applications.find @params[:application_id]
    assert_equal(@params[:application_id], application[:application_id])
    assert_equal(@params[:backend_url], application[:backend_url])
  end

  def test_update_sends_http_put
    stub_request(:put, "http://router.cluster/applications/#{@params[:application_id]}").
        with(:body => {"backend_url"=>@params[:backend_url]}).
        to_return(:status => 200, :body => @params.to_json)
    application = @router_api.applications.update @params
    assert_equal(@params[:application_id], application[:application_id])
    assert_equal(@params[:backend_url], application[:backend_url])
  end

  def test_can_delete_an_application
    application_id = "test-application"
    stub_request(:delete, "http://router.cluster/applications/#{application_id}").
        with(:headers => {'Accept'=>'*/*', 'User-Agent'=>'Ruby'}).
        to_return(:status => 200, :body => "", :headers => {})
    @router_api.applications.delete application_id
    assert_requested :delete, "http://router.cluster/applications/#{application_id}", :times => 1
  end
end
