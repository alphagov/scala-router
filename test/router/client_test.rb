require "test_helper"

class ClientTest < Test::Unit::TestCase

  def setup
    @router_client = Router::Client.new("http://router.gov.uk")

    # Create application
    stub_request(:post, "http://router.gov.uk/applications/test-application").
        with(:body => {"backend_url"=>"http://jobs.alphagov.co.uk"}).
        to_return(:status => 201, :body => '{"application_id":"test-application","backend_url":"http://jobs.alphagov.co.uk"}')

    # Get created application
    stub_request(:get, "http://router.gov.uk/applications/test-application").
        to_return(:status => 200, :body => '{"application_id":"test-application","backend_url":"http://jobs.alphagov.co.uk"}')

    # Update application
    stub_request(:put, "http://router.gov.uk/applications/test-application").
        with(:body => {"backend_url"=>"http://sausages.alphagov.co.uk"}).
        to_return(:status => 200, :body => '{"application_id":"test-application","backend_url":"http://sausages.alphagov.co.uk"}')

    # Delete application
    stub_request(:delete, "http://router.gov.uk/applications/test-application").
        with(:headers => {'Accept'=>'*/*', 'User-Agent'=>'Ruby'}).
        to_return(:status => 200, :body => "", :headers => {})
  end

  def test_can_create_update_and_delete_applications
    # Create application
    application = @router_client.create_application "test-application", "http://jobs.alphagov.co.uk"
    assert_equal("test-application", application.application_id)
    assert_equal("http://jobs.alphagov.co.uk", application.backend_url)

    # Attempt to re-create application
    stub_request(:post, "http://router.gov.uk/applications/test-application").
        with(:body => {"backend_url"=>"http://jobs.alphagov.co.uk"}).
        to_return(:status => 409, :body => '{"application_id":"test-application","backend_url":"http://jobs.alphagov.co.uk"}')

    assert_raise Router::Conflict do
      @router_client.create_application "test-application", "http://jobs.alphagov.co.uk"
    end

    # Get created application
    application = @router_client.get_application "test-application"
    assert_equal("test-application", application.application_id)
    assert_equal("http://jobs.alphagov.co.uk", application.backend_url)

    # Update application
    application = @router_client.update_application "test-application", {"backend_url" => "http://sausages.alphagov.co.uk"}
    assert_equal("test-application", application.application_id)
    assert_equal("http://sausages.alphagov.co.uk", application.backend_url)

    # Delete application
    @router_client.delete_application "test-application"
  end
end
