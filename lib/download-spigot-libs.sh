#!/bin/bash

# Check if BuildTools directory exists, create if not
if [ ! -d "BuildTools" ]; then
    mkdir BuildTools
fi
cd BuildTools

# Install Git and OpenJDK 8 on Ubuntu, if you don't have them installed already
sudo apt install git openjdk-8-jdk-headless

# You might need to update these paths to match your system's java 8 installation
export JAVA_HOME="/usr/lib/jvm/java-1.8.0-openjdk-amd64"
export PATH="$JAVA_HOME/bin:$PATH"

# Download BuildTools.jar if newer version is available
curl -o BuildTools.jar https://hub.spigotmc.org/jenkins/job/BuildTools/lastSuccessfulBuild/artifact/target/BuildTools.jar

# Function to handle the build process for each version
build_for_version() {
    local version=$1
    local version_dir="../$version"
    if [ ! -d "$version_dir" ]; then
        mkdir "$version_dir"
    fi
    cd "$version_dir"
    java -jar ../BuildTools/BuildTools.jar --rev "$2" --output-dir ../../nms
}

build_for_version "1_12_R1" "1.12.2"
build_for_version "1_13_R2" "1.13.2"
build_for_version "1_14_R1" "1.14.4"
build_for_version "1_15_R1" "1.15.2"
build_for_version "1_16_R3" "1.16.5"
