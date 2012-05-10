#!/bin/sh

pushd `dirname $0`/../
./sbt eclipse
popd