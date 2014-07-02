#!/bin/sh

#runs postprocessing
#input in $1
#output in $2. It HAS to exist, and should best be empty (we fail in the
#presence of files/directories with certain names)

#Note: both $1 and $2 have to be absolute

mkdir $2/sections/
mkdir -p $2/temp/lower-third

resources="`pwd`/resources"

cp="`pwd`/jipopro.jar"
for i in `pwd`/lib/*; do cp=${cp}:$i; done

(cd $1 && 
java -cp $cp org.jitsi.recording.postprocessing.PostProcessing --in="$1" --out="$2" --resources="$resources"
)
