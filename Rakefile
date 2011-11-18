# -*- encoding: utf-8 -*-

require "rubygems"
require "rubygems/package_task"
require 'rake/testtask'

spec = Gem::Specification.load('router-ruby-client.gemspec')

Gem::PackageTask.new(spec) do
end

Rake::TestTask.new("test") do |task|
  task.ruby_opts << "-rubygems"
  task.libs << "test"
  task.test_files = FileList["test/**/*_test.rb"]
  task.verbose = true
end

task :default => :test