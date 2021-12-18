:: This file does not need to be run for WeaponMechanics. This script is to
:: make downloading specific versions easier. Note that the output will always
:: be in the folder "manual"

@echo off
IF NOT EXIST BuildTools (
    mkdir BuildTools
)
cd BuildTools

:: Change which java version you need
set /p Input=Which java version (Probably either 1.8 or 17): || set Input="17"
set JAVA_HOME=C:\Program Files\Java\jdk-%Input%
set Path=%JAVA_HOME%\bin;%Path%

curl -z BuildTools.jar -o BuildTools.jar https://hub.spigotmc.org/jenkins/job/BuildTools/lastSuccessfulBuild/artifact/target/BuildTools.jar

echo Moving into "manual"

IF NOT EXIST manual-download (
    mkdir manual-download
)
cd manual-download

:: Choose spigot version to build. Defaults to latest STABLE release
set /p Input=Enter the version: || set Input=latest
java -jar ../BuildTools.jar --rev %Input%

PAUSE