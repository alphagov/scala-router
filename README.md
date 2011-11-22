# Ruby client gem for the .gov.uk Router

This directory contains the ruby client gem for the .gov.uk router

## Versioning

We will version this gem in a way compatible with the principles of
http://semver.org/.

## Usage

    require 'router/client'

    # Create an jobs application and route
    client = Router::Client.new
    client.applications.create(
      application_id: "jobs", 
      backend_url: "http://jobs.alphagov.co.uk")
    client.routes.create(
      application_id: "jobs",
      route_type: :prefix,
      incoming_path: "/jobs")
  
    # Lookup a route
    found_route = client.routes.find('/jobs')
    puts found_route[:application_id] # == "jobs"
    puts found_route[:route_type] # == "prefix"
    puts found_route[:incoming_path] # == "/jobs"
  
    # Error handling
    begin
      client.routes.find('/missing')
    rescue Router::NotFound => e
      # do stuff
    rescue Router::Conflict => e
      # do stuff
    end