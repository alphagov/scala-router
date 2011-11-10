#!/usr/bin/env ruby

require 'FileUtils'

$sample_html_file = 'target-html/sample.html'
$number_of_files_to_generate = 1000
$random_url_multiple = 10
$http_server_path_prefix = "router-performance-test-docroot"
$backend_url_of_router = "localhost"
$target_urls = []

full_path_directory = '../full'
prefix_directory = '../prefix'
output_url_file_for_jmeter = "../jmeter-tests/router-urls.txt"
full_route_config_file_for_jmeter = "../jmeter-tests/full-route-setup.txt"
preifx_route_config_file_for_jmeter = "../jmeter-tests/prefix-route-setup.txt"

abort("Could not open sample file #{$sample_html_file}") unless File.exists?($sample_html_file)

def clean_directory(directory)
  FileUtils.rmdir(directory)
  FileUtils.mkdir_p(directory)
end

def generate_html_files_for_performance_test(directory, type)
  config_instructions = []

  (1..$number_of_files_to_generate).each do |i|
    config_instructions.push("#{type}/#{i}.html")
    FileUtils.copy($sample_html_file, "#{directory}/#{i}.html")
  end

  $target_urls.push(config_instructions.dup)
  config_instructions
end

def generate_jmeter_url_files(output_url_file_for_jmeter)
  jmeter_urls = []
  $random_url_multiple.times.each { jmeter_urls.push($target_urls) }

  File.open(output_url_file_for_jmeter, 'w') do |index_file|
    jmeter_urls.flatten.shuffle.each do |url|
      index_file.write("router/route/#{url}\n")
    end
  end
end

def generate_config_instructions(config_file, route_urls)
  File.open(config_file, 'w') do |config|
    config.puts "router/applications?application-id=performance-test-full&backend-url=localhost"
    config.puts "router/applications?application-id=performance-test-prefix&backend-url=localhost"
    config.puts "router/add-route?application-id=performance-test-prefix&incoming-path=prefix&route-type=prefix"

    route_urls.each do |route|
      config.puts "router/add-route?application-id=performance-test-full&incoming-path=#{route}&route-type=full"
    end
  end
end

clean_directory(full_path_directory)
clean_directory(prefix_directory)

puts "Generating full route files"
config_instructions = generate_html_files_for_performance_test(full_path_directory, :full)
generate_config_instructions(full_route_config_file_for_jmeter, config_instructions)

puts "Generating prefix route files"
generate_html_files_for_performance_test(prefix_directory, :prefix)

puts "Generating random URL list for jmeter"
generate_jmeter_url_files(output_url_file_for_jmeter)