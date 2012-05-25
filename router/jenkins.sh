#!/bin/sh
set -e -x
cd ${WORKSPACE:?}/router
./sbt clean test package
