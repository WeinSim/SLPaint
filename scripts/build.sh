#!/bin/bash
set -e

echo "Building SLPaint package..."

# initialize directories
rm -rf build
rm -rf dist
mkdir -p build/package-input
mkdir dist

# create jar file
echo "Creating jar file..."
jar cf build/slpaint.jar -C bin .

# list jar file contents
# jar tf build/slpaint.jar

# test jar file
# java -cp build/slpaint.jar main.MainLoop

# copy files into package-input directory
# I don't know why but it seems like I need the jar in both places (build and build/package-input)
cp build/slpaint.jar build/package-input/
cp -r res build/package-input/
cp -r lib build/package-input/
# ln -s res build/package-input/res
# ln -s lib build/package-input/lib

# automatically determine required modules
# (doesn't really work, thanks ChatGPT)
# MODULES=$(jdeps --print-module-deps --ignore-missing-deps -cp "lib/*" build/slpaint.jar)

# create package
echo "Running jpackage command..."
# --linux-package-name slpaint \
# --install-dir slpaint \
# --license-file ../LICENSE \
# --about-url "https://github.com/WeinSim/SLPaint" \
jpackage \
    --name SLPaint \
    --app-version 0.0 \
    --description "A free and simple image editing program" \
    --copyright "Copyright 2026, Simon Weinzierl" \
    --input build/package-input \
    --main-jar slpaint.jar \
    --main-class main.MainLoop \
    --add-modules "java.base,java.desktop,jdk.unsupported" \
    --java-options "--enable-native-access=ALL-UNNAMED --sun-misc-unsafe-memory-access=allow" \
    --type app-image \
    --dest dist

echo "Done"