# Ruby client gem for the .gov.uk Router

This directory contains the ruby client gem for the .gov.uk router

## Versioning

We will version this gem in a way compatible with the principles of
http://semver.org/.

## Usage

    require 'router'

    # Create an jobs application and routes using DSL-like syntax
    router = Router.new "http://localhost:4000/router"
    jobs_app = router.application("jobs", "http://jobs.alphagov.co.uk") do |app|
      app.ensure_prefix_route "/job-search"
      app.ensure_full_route "/jobs"
    end

    # Error handling
    begin
      jobs_app.ensure_full_route "/job-search/not-allowed"
    rescue Router::Conflict => e
      puts e.existing # => {application_id: "jobs", route_type: :prefix, incoming_path: "/job-search"}
    end
