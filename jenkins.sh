#!/bin/sh
set -x -e
router/jenkins.sh
client/jenkins.sh
integration/jenkins.sh
cd ${WORKSPACE:?}/router && ./sbt clean package
