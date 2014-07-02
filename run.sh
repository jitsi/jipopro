#!/bin/sh

#runs postprocessing in $1

cp -r resources $1
mkdir $1/sections/
mkdir -p $1/temp/lower-third

cp="`pwd`/jipopro.jar"
for i in `pwd`/lib/*; do cp=${cp}:$i; done

(cd $1 && 
java -cp $cp org.jitsi.recording.postprocessing.PostProcessing --resources=resources
)
