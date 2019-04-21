JSyn
====

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

To build JSyn, use ant. Just enter:

    cd jsyn
    ant

The resulting jar will be placed in "/dist/lib/".
To run the built-in test App on a desktop, enter something like this:

    java -jar dist/lib/jsyn-20160203.jar

but with the correct date.

JSyn - Copyright 1997-2014 Mobileer Inc
