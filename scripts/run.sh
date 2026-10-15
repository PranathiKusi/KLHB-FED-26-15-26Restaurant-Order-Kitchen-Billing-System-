#!/bin/sh
# Compile and run DineSync on Linux/macOS. Usage: scripts/run.sh [--demo] [--fast]
cd "$(dirname "$0")/../src" && javac *.java && java Main "$@"
