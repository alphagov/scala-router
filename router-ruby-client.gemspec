# -*- encoding: utf-8 -*-
lib = File.expand_path('../lib/', __FILE__)
$:.unshift lib unless $:.include?(lib)

require 'router/version'

Gem::Specification.new do |spec|
  spec.name = "router-ruby-client"
  spec.version = "0.1"
  spec.platform = Gem::Platform::RUBY
  spec.authors = ["Mat Wall"]
  spec.email = ["team@alphagov.gov.uk"]
  spec.summary = "HTTP client for gov.uk router API"
  spec.description = "Create / manage configured routes in the gov.uk router"

  spec.files = Dir.glob("lib/**/*") + %w(README.md)
  spec.require_path = 'lib'
  spec.add_runtime_dependency 'builder'

  spec.test_files = Dir['test/**/*']
  spec.add_development_dependency 'test-unit'
  spec.add_development_dependency 'webmock'
end
