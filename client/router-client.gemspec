lib = File.expand_path('../lib/', __FILE__)
$:.unshift lib unless $:.include?(lib)

require 'router/version'

Gem::Specification.new do |spec|
  spec.name = "router-client"
  spec.version = Router::VERSION
  spec.platform = Gem::Platform::RUBY
  spec.authors = ["Mat Wall", "Craig R Webster"]
  spec.email = ["team@alphagov.gov.uk", "craig@barkingiguana.com"]
  spec.summary = "HTTP client for gov.uk router API"
  spec.description = "Create / manage configured routes in the gov.uk router"

  spec.files = Dir.glob("lib/**/*") + %w(README.md)
  spec.require_path = 'lib'
  spec.add_runtime_dependency 'builder'
  spec.add_runtime_dependency 'null_logger'
  spec.add_runtime_dependency 'activesupport'

  spec.test_files = Dir['test/**/*']
  spec.add_development_dependency 'rake'
  spec.add_development_dependency 'gemfury'
  spec.add_development_dependency 'gem_publisher', '~> 1.0.0'
  spec.add_development_dependency 'mongo', '1.5.2'
  spec.add_development_dependency 'simplecov'
end
