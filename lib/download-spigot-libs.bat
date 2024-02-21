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

:: Subroutine for building a specific version
:buildVersion
IF NOT EXIST "../%1" (
    mkdir "../%1"
)
cd "../%1"
java -jar ../BuildTools/BuildTools.jar --rev %2 --output-dir ../../nms
goto :eof

:: Call the buildVersion subroutine with the version directory and version number as arguments
call :buildVersion "1_12_R1" "1.12.2"
call :buildVersion "1_13_R2" "1.13.2"
call :buildVersion "1_14_R1" "1.14.4"
call :buildVersion "1_15_R1" "1.15.2"
call :buildVersion "1_16_R3" "1.16.5"