#!/usr/bin/env bash
./gradlew functionaltest;
STATUS_CODE="$?"
mkdir -p $CIRCLE_TEST_REPORTS/functionaltest/;
find . -type f -regex ".*/build/test-results/functionaltest/.*xml" -exec cp {} $CIRCLE_TEST_REPORTS/functionaltest/ \;
exit $STATUS_CODE