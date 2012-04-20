#! /bin/bash

cd `dirname $0`
bundle install --path "${HOME}/bundles/${JOB_NAME}"
bundle exec rake
