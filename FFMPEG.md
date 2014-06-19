ffmpeg needs to be compiled with
* --enable-libfreetype (for drawtext)
* --enable-libvpx (for encoding the final result)
* --enable-libvorbis (for encoding the final result, in case of webm)
* --enable-libmp3lame (for decoding the input audio)
* --enable-libx264 (for encoding the final result, in case of mp4)

For MacOSX 10.9, the version from macports does not work. The build here seems to mostly work:
http://www.evermeet.cx/ffmpeg/



## Building ffmpeg on ubuntu:
### Dependencies:
#### From packages
> apt-get install libxft-dev libmp3lame-dev libvorbis-dev

#### libvpx from git, because ubuntu has a very old version (1.0.0)
Get the sources:
>git clone https://chromium.googlesource.com/webm/libvpx

Configure with:
> ./configure --disable-docs --disable-examples --disable-vp9  --disable-unit-tests --enable-shared --enable-error-concealment

Build with just:
> make


### ffmpeg
Get the sources:
> git clone git://source.ffmpeg.org/ffmpeg.git

Optional: apply the patch to enable vp8 error-concealment (from the 'patches' directory):
> git apply 0001-Enable-vp8-decoder-error-concealment.patch


Configure with:
> ./configure --enable-libfreetype --enable-libvpx --enable-libvorbis --enable-libmp3lame
>    --enable-libx264 --enable-gpl
>    --extra-cflags=-Ipath-to-libvpx --extra-ldflags=-Lpath-to-libvpx

Build with just:
> make
