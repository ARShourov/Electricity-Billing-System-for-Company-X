#!/usr/bin/env sh
# Compile and start the app (macOS / Linux). Needs JDK 17+ on PATH.
cd "$(dirname "$0")" || exit 1
mkdir -p bin
find src -name '*.java' > sources.txt
javac -encoding UTF-8 -d bin @sources.txt || { echo "Compile failed. Is the JDK installed? Check: javac -version"; rm -f sources.txt; exit 1; }
rm -f sources.txt
java -cp "bin:src" electricity.billing.system.Splash
