# JSyn

JSyn is a modular audio synthesizer for Java by Phil Burk.

You can use JSyn to create unit generators, such as oscillators, filters,
and envelopes. Units can be connected together and controlled
in real-time from a Java program.

More information about JSyn, including documentation, is at:

http://www.softsynth.com/jsyn/

Pre-compiled JSyn JAR files are at:

https://github.com/philburk/jsyn/releases
and
http://www.softsynth.com/jsyn/developers/download.php

The JSyn source code is available at:

https://github.com/philburk/jsyn

## Using JSyn in Your Project

If your project is not using gradle then build or [download](https://github.com/philburk/jsyn/releases)
the JSyn jar file and add it to your classpath. See Build section below.

If you are using gradle then you can simply add a dependency on JSyn to your project.
The easiest way is to use jitpack to add the latest release from GitHub.

For Groovy gradle files use:

    repositories {
        maven { url  "https://jitpack.io" }
    }

    dependencies {
        implementation "com.github.philburk:jsyn:latest.release"
    }

The syntax is slightly different if you are using ".kts" files.

    repositories {
        maven { url = uri("https://jitpack.io") }
    }

    dependencies {
        implementation("com.github.philburk:jsyn:latest.release")
    }

## Building JSyn

You can build JSyn using either Ant or Gradle.

Note that if you clone the repository then you will end up with a folder called "jsyn". But if you download the ZIP file it will be called "jsyn-master".

### Build Using Ant

You may need to install Ant first. On Mac you can do:

    brew install ant

Then enter:

    cd jsyn   # or jsyn-master
    ant

The resulting jar will be placed in "dist/lib/".

To run the built-in test App on a desktop, enter something like this:

    java -jar dist/lib/jsyn-20230410.jar

but with the correct date.

To create javadocs enter:

    ant doc

### Build Using Gradle

Enter:

    cd jsyn   # or jsyn-master
    ./gradlew assemble

The resulting jar will be placed in "build/libs/".
To run the built-in test App on a desktop, enter something like this:

    java -jar build/libs/jsyn-17.2.0.jar

but with the correct version.

### Test Using Gradle

To run the unit tests, enter:

    cd jsyn
    ./gradlew test

## How to Release a new version of JSyn

1. Update the version, BUILD_NUMBER and BUILD_DATE in src/main/java/com/jsyn/JSyn.java
2. Update the version in build.gradle
3. Check out the latest code locally.
4. Build the Jar using the Gradle instructions above.
5. Test using the instructions above.
6. In one window, go to the latest release, eg. https://github.com/philburk/jsyn/releases/tag/v17.1.0
7. Click "{N} commits to master since this release"
8. In a second window, go to https://github.com/philburk/jsyn/releases and click "Draft a New Release".
9. Write release notes based on the new commit list.
10. Drag and drop the JAR file created by gradle to the Release page.
11. Click "Publish Release".
