#!/bin/bash
set -e

echo "Building SLPaint package..."

# initialize directories
rm -rf build/package-input
rm -rf dist
mkdir build/package-input
mkdir dist

# create jar file
echo "Creating jar file..."
jar cf build/package-input/slpaint.jar \
    -C bin . \
    -C res .

# list jar file contents
# jar tf build/package-input/slpaint.jar

# test jar file
# java -cp build/package-input/slpaint.jar main.MainLoop

# copy files into package input directory
cp -r lib build/package-input/

# automatically determine required modules
# (doesn't really work, thanks ChatGPT)
# MODULES=$(jdeps --print-module-deps --ignore-missing-deps -cp "lib/*" build/package-input/slpaint.jar)

# create package
echo "Running jpackage command..."
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
    --dest dist \
    --about-url "https://github.com/WeinSim/SLPaint" \
    --linux-package-name slpaint \
    --icon res/logo/logo_256.png \
    --file-associations build/png.properties \
    --file-associations build/jpg.properties \
    --file-associations build/jpeg.properties \
    --file-associations build/bmp.properties \
    --linux-deb-maintainer "simon-weinzierl@web.de" \
    --linux-menu-group "Graphics" \
    # --type app-image

echo "Done"