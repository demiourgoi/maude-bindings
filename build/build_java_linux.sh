#!/bin/bash

set -xe

SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

${SCRIPT_DIR}/build_linux_start.sh

# here is config.h
cd subprojects/maudesmc/build

cmake ../../.. -DLANGUAGE=java -DBUILD_LIBMAUDE=OFF
cmake --build .

JAVA_DIST=../../../dist/java
rm -rf $JAVA_DIST && mkdir -p $JAVA_DIST 
cp maudejni.jar $JAVA_DIST 
cp libmaudejni.so $JAVA_DIST 

echo "Done: see artifacts at maude-bindings/dist/java"
