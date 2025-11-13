# Maude Java library 

See [Javadoc documentation](https://fadoss.github.io/maude-bindings/javadoc/es/ucm/maude/bindings/package-summary.html)

## How to build

First generate the native libraries following section "Building Java bindings (WIP)" of ../README.md. Also update the code in lib/src/main/java/ as needed: note package names might be renamed in order to have publishing permissions on Maven Central.

Then build as follows:

```bash
# add the native libraries
cp ../dist/java/*.so  lib/src/main/resources/native/linux/
# make sure in lib/src/main/resources/maude/stdlib you have all the .maude
# files corresponding to the [Maude release](https://github.com/maude-lang/Maude/releases)
# that makes the native libraries

# tests
make clean build

# check the jar contains the native libs
jar tf lib/build/libs/lib.jar | grep "native"
```

To publish the library to ~/.m2/repository/es/ucm/maude/bindings use

```bash
make clean release
```

For __release versioning__ we use the Maude release version plus an additional patch build to identify the bindings release themselves, see [build.gradle](./lib/build.gradle) for details.


## Limitations

The Maude prelude and Maude stdlib is added to this jar, and the prelude is loaded by calling `MaudeRuntime.init()`. Note this must be done BEFORE loading any standard module like e.g. `maude.getModule("NAT");`. . See that class and [MaudeRuntimeTest](./lib/src/test/java/es/ucm/maude/bindings/MaudeRuntimeTest.java) for how to load other Maude from the Maude standard library, and in general from jar resources.

Currently this library __only supports Linux x86_64__. For development on other platforms consider using Docker.

```bash
$ file ../dist/java/libmaude.so 
../dist/java/libmaude.so: ELF 64-bit LSB shared object, x86-64, version 1 (SYSV), dynamically linked, BuildID[sha1]=277c22f13f05f9d990bc37d5ec4179ac7df8d909, stripped
$ file ../dist/java/libmaudejni.so 
../dist/java/libmaudejni.so: ELF 64-bit LSB shared object, x86-64, version 1 (GNU/Linux), dynamically linked, BuildID[sha1]=a2fe0d6a25174e2dbfc608f3b0e645c815889763, not stripped
```
