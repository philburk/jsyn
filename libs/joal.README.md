# JOAL, OpenAL API Binding for Java™

[Original document location](https://jogamp.org/cgit/joal.git/about/)

## Git Repository
This project's canonical repositories is hosted on [JogAmp](https://jogamp.org/cgit/joal.git/).

## Overview
The [*JOAL Project*](https://jogamp.org/joal/www/) hosts a reference implementation of the
Java bindings for [OpenAL API](http://www.openal.org/), and is designed to provide
hardware-supported 3D spatialized audio for applications written in Java.

This project also hosts the Sound3D Toolkit, a high level
API for spatialized audio built on top of the OpenAL bindings.
This toolkit is designed to provide access to all the features
of OpenAL through an intuitive, easy to use, object-oriented interface.

JOAL is part of [the JogAmp project](https://jogamp.org).

**The JogAmp project needs funding and we offer [commercial support](https://jogamp.org/wiki/index.php?title=Maintainer_and_Contacts#Commercial_Support)!**<br/>
Please contact [Göthel Software (Jausoft)](https://jausoft.com/).

### License
See [LICENSE.txt](LICENSE.txt).

## Platform Support
JOAL is tested against [OpenAL-Soft](https://openal-soft.org/) ([github repo](https://github.com/kcat/openal-soft/)),
the cross-platform, software implementation of the OpenAL 3D audio API.

See OpenAL-Soft [environment variables](https://github.com/kcat/openal-soft/blob/master/docs/env-vars.txt)
and [configuration example](https://github.com/kcat/openal-soft/blob/master/alsoftrc.sample).

All JOAL platform builds contain a self-build native library of [OpenAL-Soft](https://openal-soft.org/),
version [**v1.23.1** *from our fork*](https://jogamp.org/cgit/openal-soft.git/).

Our builds expose the following audio backends:

### GNU/Linux
- PipeWire
- PulseAudio
- ALSA
- OSS
- SndIO (linked)
- WaveFile
- Null

### Android/Linux
- PipeWire
- OpenSL
- WaveFile
- Null

### Windows
- WinMM
- DirectSound
- WASAPI
- WaveFile
- Null

### MacOS
- CoreAudio
- WaveFile
- Null

## Build Requirements
This project has been built under Win32, GNU/Linux, Android/Linux and MacOS. 

Check [GlueGen's HowToBuild](https://jogamp.org/gluegen/doc/HowToBuild.html)
for basic prerequisites.

Additionally the following packages and tools have been used:

* All Systems:
  - See [GlueGen's HowToBuild](https://jogamp.org/gluegen/doc/HowToBuild.html)
  - [OpenAL-Soft](https://openal-soft.org/) ([github repo](https://github.com/kcat/openal-soft/))

* Windows:
  - [CMake 3.15.2](https://cmake.org/download/)
  - OpenAL Soft: Audio-Backends: WinMM, DirectSound, WASAPI, WaveFile, Null

* GNU/Linux:
  - cmake
  - OpenAL Soft: OpenAL: PipeWire, PulseAudio, ALSA, OSS, SndIO (linked), WaveFile, Null
```
     apt-get install cmake autoconf \
             libpipewire-0.3-dev \
             libpulse-dev libpulse0:amd64 libpulse0:i386 pulseaudio \
             libsndio-dev \
             libasound2-dev libasound2:amd64 libasound2:i386
```
 On Debian 11 Bullseye, use bullseye-backports `apt -t bullseye-backports install libpipewire-0.3-dev`
 to have libpipewire-0.3>=0.3.23, i.e. version 0.3.65.

* Android/Linux:
  - cmake
  - OpenAL Soft: Audio-Backends: PipeWire, OpenSL, WaveFile, Null

* OSX
  - OSX 10.2 or later
  - OSX Developer Tools Xcode
  - CMake 3.15.2 <https://cmake.org/download/> 
    and install the commandline tools <https://stackoverflow.com/questions/30668601/installing-cmake-command-line-tools-on-a-mac>
  - OpenAL Soft: Audio-Backends: CoreAudio, WaveFile, Null

JOAL requires the GlueGen workspace to be checked out as a sibling
directory to the joal directory. 
See GlueGen's HowToBuild <https://jogamp.org/gluegen/doc/HowToBuild.html>

## Directory Organization:
```
make/           Build-related files and the main build.xml
src/            The actual source for the JOAL APIs.
src/test/       A couple of small tests
build/          (generated directory) Where the Jar and DLL files get built to
www/            JOAL project webpage files
```

## GIT
JOAL can be build w/ openal-soft, which is a git submodule of JOAL.
This is the default for our JogAmp build on all platforms.

Cloning [and pulling] JOAL incl. openal-soft 
can be performed w/ the option '--recurse-submodules'.
```
   > cd /home/dude/projects/jogamp/
   > git clone --recurse-submodules git://jogamp.org/srv/scm/joal.git
   > cd joal ; git pull --recurse-submodules
```
 
## JOAL Build Instructions:
Change into the joal/make directory
```
   > cd /home/dude/projects/jogamp/make/
```

To clean: 
```
   > ant clean
```

To build:
```
   > ant -Dtarget.sourcelevel=1.8 -Dtarget.targetlevel=1.8 -Dtarget.rt.jar=/your/openjdk8/lib/rt.jar
```

To build docs:
```
   > ant -Dtarget.sourcelevel=1.8 -Dtarget.targetlevel=1.8 -Dtarget.rt.jar=/your/openjdk8/lib/rt.jar javadoc
```

To test:
```
   > ant -Dtarget.sourcelevel=1.8 -Dtarget.targetlevel=1.8 -Dtarget.rt.jar=/your/openjdk8/lib/rt.jar runtests
```

Instead of properties, you may also use environment variables, 
see GlueGen's HowToBuild <https://jogamp.org/gluegen/doc/HowToBuild.html>.

## Contact Us
- JogAmp             [http://jogamp.org/](http://jogamp.org/)
- JOAL Web           [http://jogamp.org/](http://jogamp.org/joal/)
- Forum/Mailinglist  [http://forum.jogamp.org/](http://forum.jogamp.org/)
- Repository         [http://jogamp.org/git/](http://jogamp.org/git/)
- Wiki               [https://jogamp.org/wiki/](https://jogamp.org/wiki/)
- Maintainer         [https://jogamp.org/wiki/index.php/Maintainer_and_Contacts](https://jogamp.org/wiki/index.php/Maintainer_and_Contacts)
- Sven's Blog        [https://jausoft.com/blog/tag/jogamp/](https://jausoft.com/blog/tag/jogamp/)
- Email              sgothel _at_ jausoft _dot_ com

## Acknowledgments
Original JOAL and Sound3D authors

- Athomas Goldberg
- Wildcard
- Java Games Initiative
- Software Advanced Technologies Group
- Sun Microsystems

Since roughly 2010, JOAL development has been continued
by individuals of the JogAmp community, see git log for details.

