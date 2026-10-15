#!/bin/sh
# Compile and run all automated tests on Linux/macOS.
cd "$(dirname "$0")/../src" && javac *.java && java DineSyncTests
