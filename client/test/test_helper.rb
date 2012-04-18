require "rubygems"
require "bundler/setup"

require "test/unit"
require "webmock/test_unit"
require "router/client"
require "router"
require "cgi"
require "mocha"

WebMock.disable_net_connect!
