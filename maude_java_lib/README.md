# Maude Java library 

See [Javadoc documentation](https://fadoss.github.io/maude-bindings/javadoc/es/ucm/maude/bindings/package-summary.html)

## How to build

First generate the native libraries following section "Building Java bindings (WIP)" of ../README.md. Also update the code in lib/src/main/java/ as needed: note package names might be renamed in order to have publishing permissions on Maven Central.

Then build as follows:

```bash
# add the native libraries
cp ../dist/java/*.so  lib/src/main/resources/META-INF/native/linux/

# FIXME make prelude.maude available so `maude.getModule("NAT")` does not fail on lib/src/test/java/es/ucm/maude/bindings/HelloWorldTest.java
export MAUDE_LIB=${HOME}/systems/maude/latest

# tests
make clean build

# check the jar contains the native libs
jar tf lib/build/libs/lib.jar | grep "META-INF/native"
```

## Limitations

Currently this library only works on Linux x86_64. For development on other platforms consider using Docker.

```bash
$ file ../dist/java/libmaude.so 
../dist/java/libmaude.so: ELF 64-bit LSB shared object, x86-64, version 1 (SYSV), dynamically linked, BuildID[sha1]=277c22f13f05f9d990bc37d5ec4179ac7df8d909, stripped
$ file ../dist/java/libmaudejni.so 
../dist/java/libmaudejni.so: ELF 64-bit LSB shared object, x86-64, version 1 (GNU/Linux), dynamically linked, BuildID[sha1]=a2fe0d6a25174e2dbfc608f3b0e645c815889763, not stripped
```
