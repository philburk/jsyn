#!/bin/bash

# set -x

#
# See https://github.com/kcat/openal-soft/blob/master/docs/env-vars.txt
# export ALSOFT_LOGLEVEL=3
#

javaexe=`which java`

rm -f java-run.log

function jrun() {
    #D_ARGS="-Djogamp.debug=all"
    #D_ARGS="-Djogamp.debug.Bitstream"
    #D_ARGS="-Djogamp.debug.NativeLibrary=true -Djoal.debug=true"
    #D_ARGS="-Djogamp.debug.AudioSink"
    #D_ARGS="-Djogamp.debug.AudioSink -Djoal.debug.AudioSink.trace"
    #D_ARGS="-Djoal.debug.AudioSink.trace"
    #D_ARGS="-Djoal.debug=all"
    #D_ARGS="-Djogamp.debug.JNILibLoader"
    #D_ARGS="-Djogamp.debug.NativeLibrary=true -Djogamp.debug.JNILibLoader=true"
    #X_ARGS="-verbose:jni"
    #X_ARGS="-Xrs"

    # StartFlightRecording: delay=10s,
    # FlightRecorderOptions: stackdepth=2048
    # Enable remote connection to jmc: jcmd <PID> ManagementAgent.start jmxremote.authenticate=false jmxremote.ssl=false jmxremote.port=7091
    # X_ARGS="-XX:+UnlockDiagnosticVMOptions -XX:+DebugNonSafepoints $X_ARGS"
    # X_ARGS="-XX:FlightRecorderOptions=stackdepth=2048,threadbuffersize=16k $X_ARGS"
    # X_ARGS="-XX:StartFlightRecording=delay=10s,dumponexit=true,filename=java-run.jfr $X_ARGS"

    for i in dist/lib/jsyn-????????.jar ; do 
      jsynjar=$i
    done
    for i in dist/lib/jsyn-examples-????????.jar ; do 
      jsynexamplesjar=$i
    done
    echo Using jsyn $jsynjar
    echo Using jsyn-examples $jsynexamplesjar

    #CLASSPATH=libs/gluegen-rt.jar:libs/joal.jar:$jsynjar:$jsynexamplesjar
    CLASSPATH=libs/jogamp-fat.jar:$jsynjar:$jsynexamplesjar

    echo
    echo "Test Start: $*"
    echo
    echo "$javaexe" $X_ARGS -cp $CLASSPATH $D_ARGS $C_ARG $*
    "$javaexe" $X_ARGS -cp $CLASSPATH $D_ARGS $C_ARG $*
    echo
    echo "Test End: $*"
    echo
}

function testnormal() {
    jrun $* 2>&1 | tee -a java-run.log
}

testnormal com.jsyn.apps.AboutJSyn
#testnormal com.jsyn.examples.PlayNotes
#testnormal com.jsyn.examples.PlayFunction

