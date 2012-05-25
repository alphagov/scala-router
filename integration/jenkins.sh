#! /bin/bash
set -e -x
cd ${WORKSPACE:?}/integration
./sbt clean test package
