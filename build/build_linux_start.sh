#!/bin/sh

set -xe

AUXFILES_PKG="https://github.com/fadoss/maude-bindings/releases/download/0.1/manylinux_2_28-auxfiles.tar.xz"
LIBMAUDE_PKG="https://github.com/fadoss/maudesmc/releases/download/latest/libmaude-manylinux_2_28.tar.xz"

curl -L "$AUXFILES_PKG" -O && \
    xz -cd $(basename "$AUXFILES_PKG") | tar -xC /

curl -L "$LIBMAUDE_PKG" -O && \
    mkdir -p libmaude-pkg && \
    xz -cd $(basename "$LIBMAUDE_PKG")  | tar -xC libmaude-pkg


mkdir -p subprojects/maudesmc/build && \
     mkdir -p subprojects/maudesmc/installdir/lib && \
     mv libmaude-pkg/config.h subprojects/maudesmc/build && \
     mv libmaude-pkg/libmaude.so subprojects/maudesmc/installdir/lib

