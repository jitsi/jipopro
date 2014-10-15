jipopro
=======

Jitsi Videobridge Post Processing (JIPOPRO) for Recorded sessions


## Requirements:
* apache ant to build
* ffmpeg (see FFMPEG.md for details)
* sox with mp3 support (on debian/ubuntu install 'sox' and 'libsox-fmt-mp3')
* phantomjs (to run without X11, version >= 1.5 is required)
* mkvinfo from mkvtoolnix (on debian/ubuntu install 'mkvtoolnix')

They all need to be in PATH.

## Building:
To rebuild just run
>ant rebuild

## Running:
To run in _DIR_ you can use either
> ./run.sh _DIR_

or

> ant run -DrunDir=_DIR_
