#!/bin/bash

filename='labels.txt'
filelines=`cat $filename |  tr -d "\r"`

for line in $filelines ; do
    ./converter --mat fbx_material.sfm --outdir sampledata models/$line.fbx
done