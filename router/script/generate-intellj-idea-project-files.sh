#!/bin/bash

pushd `dirname $0`/../
./sbt gen-idea
popd
