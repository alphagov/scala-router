# Ruby client gem for the .gov.uk Router

This directory contains the ruby client gem for the .gov.uk router

## Versioning

We will version this gem in a way compatible with the principles of
http://semver.org/.

## Usage

    require 'router/client'

    # Optionally provide a logger to the HTTP client
    require 'logger'
    logger = Logger.new STDOUT
    logger.level = Logger::DEBUG

    # Optionally create an HTTP client that points to the router location
    http_client = Router::HttpClient.new "http://localhost:4000/router", logger

    # Create an jobs application and route
    client = Router::Client.new http_client
    client.applications.create(
      application_id: "jobs",
      backend_url: "http://jobs.alphagov.co.uk")
    client.routes.create(
      application_id: "jobs",
      route_type: :prefix,
      incoming_path: "/jobs")

    # Lookup a route
    found_route = client.routes.find('/jobs')
    puts found_route[:application_id] # => "jobs"
    puts found_route[:route_type]     # => :prefix
    puts found_route[:incoming_path]  # => "/jobs"

    # Error handling
    begin
      client.routes.create(
        application_id: "jobs",
        route_type: :prefix,
        incoming_path: "/jobs")
    rescue Router::Conflict => e
      # do stuff
    end

    begin
      client.routes.delete('/missing')
    rescue Router::NotFound => e
      # do stuff
    end
