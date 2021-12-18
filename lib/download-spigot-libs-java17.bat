@echo off
IF NOT EXIST BuildTools (
    mkdir BuildTools
)
cd BuildTools

:: You need to be running java 16 or 17 to compile
set JAVA_HOME=C:\Program Files\Java\jdk-17
set Path=%JAVA_HOME%\bin;%Path%

curl -z BuildTools.jar -o BuildTools.jar https://hub.spigotmc.org/jenkins/job/BuildTools/lastSuccessfulBuild/artifact/target/BuildTools.jar

:: 1_17_R1
IF NOT EXIST 1_17_R1 (
    mkdir 1_17_R1
)
cd 1_17_R1
java -jar ../BuildTools.jar --rev 1.17.1 --output-dir ../../nms