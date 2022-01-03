@echo off
IF NOT EXIST BuildTools (
    mkdir BuildTools
)
cd BuildTools

:: You need to be running in java JDK 1.8 in order to run buildtools. If you are
:: running a newer version of java, simply uncomment these lines and add your own
:: java path here. NOTE: This will NOT change any of your environment variables!
set JAVA_HOME=C:\Program Files\Java\jdk-1.8
set Path=%JAVA_HOME%\bin;%Path%

curl -z BuildTools.jar -o BuildTools.jar https://hub.spigotmc.org/jenkins/job/BuildTools/lastSuccessfulBuild/artifact/target/BuildTools.jar

:: 1_8_R3
IF NOT EXIST 1_8_R3 (
    mkdir 1_8_R3
)
cd 1_8_R3
java -jar ../BuildTools.jar --rev 1.8.8 --output-dir ../../nms


:: 1_9_R2
IF NOT EXIST "../1_9_R2" (
    mkdir "../1_9_R2"
)
cd "../1_9_R2"
java -jar ../BuildTools.jar --rev 1.9.4 --output-dir ../../nms


:: 1_10_R1
IF NOT EXIST "../1_10_R1" (
    mkdir "../1_10_R1"
)
cd "../1_10_R1"
java -jar ../BuildTools.jar --rev 1.10.2 --output-dir ../../nms


:: 1_11_R1
IF NOT EXIST "../1_11_R1" (
    mkdir "../1_11_R1"
)
cd "../1_11_R1"
java -jar ../BuildTools.jar --rev 1.11.2 --output-dir ../../nms


:: 1_12_R1
IF NOT EXIST "../1_12_R1" (
    mkdir "../1_12_R1"
)
cd "../1_12_R1"
java -jar ../BuildTools.jar --rev 1.12.2 --output-dir ../../nms


:: 1_13_R2
IF NOT EXIST "../1_13_R2" (
    mkdir "../1_13_R2"
)
cd "../1_13_R2"
java -jar ../BuildTools.jar --rev 1.13.2 --output-dir ../../nms


:: 1_14_R1
IF NOT EXIST "../1_14_R1" (
    mkdir "../1_14_R1"
)
cd "../1_14_R1"
java -jar ../BuildTools.jar --rev 1.14.4 --output-dir ../../nms


:: 1_15_R1
IF NOT EXIST "../1_15_R1" (
    mkdir "../1_15_R1"
)
cd "../1_15_R1"
java -jar ../BuildTools.jar --rev 1.15.2 --output-dir ../../nms


:: 1_16_R3
IF NOT EXIST "../1_16_R3" (
    mkdir "../1_16_R3"
)
cd "../1_16_R3"
java -jar ../BuildTools.jar --rev 1.16.5 --output-dir ../../nms