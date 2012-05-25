#!/bin/sh
set -x -e

artifact="${WORKSPACE:?}}/router/router/target/scala-2.9.1/router_2.9.1-0.1.0-SNAPSHOT.war"
rm -f "$artifact"

router/jenkins.sh
client/jenkins.sh
integration/jenkins.sh

echo "post-build artifact stashed by Jenkins for the 'Deploy Router' project:"
md5sum "$artifact"
