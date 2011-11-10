#!/bin/bash

if [ -z "$JMETER_HOME" ]; then
	echo "You don't have JMETER_HOME set. Please set this to the root of your jmeter install"
	exit 1
fi

rm -f jmeter.log
echo "Starting performance test"
${JMETER_HOME}/bin/jmeter -n -t router-performance-tests.jmx
echo "Finished performance test. Logs can be found in jmeter.log"
