# Router Performance Tests

## Running the tests

To run the tests you'll need to have the router running on port 4000.

To do that you'll need Mongo running on your machine. This is left as an
exercise to the reader. Afterwards, normally you can do this in the terminal to
start the router:

  $ cd /path/to/router
  $ ./sbt
  clean
  jetty-run

You'll need to keep that terminal window open.

To run the tests you can now do this:

  $ cd /path/to/router/router-performance-tests
  $ rake

When the tests are complete you'll see a file, `router-performance-report.txt`,
appear in the current directory which contains the test results. You'll also get
a `jmeter.log` which will let you know what jMeter did during the test run.

## Reading the test results

I'm not really sure how to draw a meaningful result from the files that have
been output. I will fill this section in when I know.
