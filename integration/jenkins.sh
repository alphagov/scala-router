#! /bin/bash
set -e -x
cd ${WORKSPACE:?}/integration
bundle install --path "${HOME}/bundles/${JOB_NAME:?}"
bundle exec rake
