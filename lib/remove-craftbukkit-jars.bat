@echo off
cd nms

:: You need to be running in java JDK 1.8 in order to run buildtools. If you are
:: running a newer version of java, simply uncomment these lines and add your own
:: java path here. NOTE: This will NOT change any of your environment variables!
set JAVA_HOME=C:\Program Files\Java\jdk-1.8
set Path=%JAVA_HOME%\bin;%Path%

echo Deleting craftbukkit jar files in nms

:: We only need to delete jar files from 1.8 -> 1.13.2 since 1.14 stopped
:: building them by default.
del "craftbukkit-1.8.8.jar"
del "craftbukkit-1.9.2.jar"
del "craftbukkit-1.9.4.jar"
del "craftbukkit-1.10.2.jar"
del "craftbukkit-1.11.2.jar"
del "craftbukkit-1.12.2.jar"
del "craftbukkit-1.13.jar"
del "craftbukkit-1.13.2.jar"

echo Deleted craftbukkit jars
PAUSE