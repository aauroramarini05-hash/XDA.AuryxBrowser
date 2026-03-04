@rem Gradle wrapper script for Windows
@rem
@if "%DEBUG%"=="" @echo off
@setlocal

set DEFAULT_JVM_OPTS="-Xmx2048m" "-Xms256m"

set DIRNAME=%~dp0
if "%DIRNAME%"=="" set DIRNAME=.

set APP_HOME=%DIRNAME%
set APP_NAME="Gradle"
set GRADLE_WRAPPER_JAR=%APP_HOME%\gradle\wrapper\gradle-wrapper.jar

@rem Find java.exe
if defined JAVA_HOME goto findJavaFromJavaHome

set JAVA_EXE=java.exe
goto execute

:findJavaFromJavaHome
set JAVA_HOME=%JAVA_HOME:"=%
set JAVA_EXE=%JAVA_HOME%/bin/java.exe

:execute
"%JAVA_EXE%" %DEFAULT_JVM_OPTS% -jar "%GRADLE_WRAPPER_JAR%" %*

:end
@endlocal
