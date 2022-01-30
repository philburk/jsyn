# JSyn

JSyn is a modular audio synthesizer for Java by Phil Burk.

You can use JSyn to create unit generators, such as oscillators, filters,
and envelopes. Units can be connected together and controlled
in real-time from a Java program.

More information about JSyn, including documentation, is at:

http://www.softsynth.com/jsyn/

Pre-compiled JSyn JAR files are at:

http://www.softsynth.com/jsyn/developers/download.php

The JSyn source code is available at:

https://github.com/philburk/jsyn

## Building JSyn

You can build JSyn using either Ant or Gradle.

### Build Using Ant

Enter:

    cd jsyn
    ant

The resulting jar will be placed in "dist/lib/".
    
To run the built-in test App on a desktop, enter something like this:

    java -jar dist/lib/jsyn-20160203.jar

but with the correct date.

To create javadocs enter:

    ant docs

### Build Using Gradle

Enter:

    cd jsyn
    ./gradlew assemble
    
The resulting jar will be placed in "build/libs/".
To run the built-in test App on a desktop, enter something like this:

    java -jar build/libs/jsyn-17.0.0-SNAPSHOT.jar
    
but with the correct version.

### Test Using Gradle

To run the unit tests, enter:

    cd jsyn
    ./gradlew test
    
JSyn - Copyright 1997 Mobileer Inc
