#!/bin/bash

# Function to execute the Java command for building
build_minecraft_version() {
    if [ ! -d "$1" ]; then
        mkdir "$1"
    fi
    cd "$1"
    /usr/lib/jvm/java-8-openjdk/jre/bin/java -jar ../BuildTools.jar --rev $2 --output-dir ../../nms
    cd ..
}

# Building different Minecraft versions
build_minecraft_version "1_9_R2" "1.9.4"
build_minecraft_version "1_10_R1" "1.10.2"
build_minecraft_version "1_11_R1" "1.11.2"
build_minecraft_version "1_12_R1" "1.12.2"
build_minecraft_version "1_13_R2" "1.13.2"
build_minecraft_version "1_14_R1" "1.14.4"
build_minecraft_version "1_15_R1" "1.15.2"
build_minecraft_version "1_16_R3" "1.16.5"
