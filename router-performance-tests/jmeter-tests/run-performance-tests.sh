#!/bin/bash

if [ -z "$JMETER_HOME" ]; then
	echo "You don't have JMETER_HOME set. Please set this to the root of your jmeter install"
	exit 1
fi

TESTS=${1-router-performance-tests.jmx}

rm -f jmeter.log
echo "Starting performance test $TESTS"
${JMETER_HOME}/bin/jmeter -n -t $TESTS
echo "Finished performance test. Logs can be found in jmeter.log"
