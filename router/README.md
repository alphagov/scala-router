# Scala router spike and performance tests

This directory contains the scala router spike and the jmeter performance tests to run against the router.

# Running the scala router

Ensure that you have a Java 1.6.0 JDK on your machine.

First, you are going to have to install mongodb. The router requires version 2.0 or above of mongodb configured in
a replica set. Waargh! Don't worry, however, there is a script to do all of this for you. (Note: If you already have a 
running mongodb on your machine you should stop it before running this step)

To configure mongodb enter the following in a shell:

    cd mongodb
    ./router-single-node.sh

To build and run the router itself, enter the following in a shell:

    ./sbt
    test
    jetty-start

You should now have a scala router running on port 8080 on your local machine.

# To run the performance tests

Install jmeter somewhere.

    cd router-performance-tests/jmeter-tests
    JMETER_HOME=path-to-jmeter-home ./run-performance-tests.sh


