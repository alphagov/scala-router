#!/bin/sh

cd `dirname $0`
./sbt clean test package
