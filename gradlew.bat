@rem Gradle startup script for Windows.
@rem If gradle\wrapper\gradle-wrapper.jar is missing, open this project in
@rem Android Studio instead -- it does not need this script to sync/build/run.

@echo off
set DIRNAME=%~dp0
set APP_HOME=%DIRNAME%

if exist "%APP_HOME%gradle\wrapper\gradle-wrapper.jar" goto runWrapper

echo gradle\wrapper\gradle-wrapper.jar is not present.
echo Open this project directly in Android Studio, or run "gradle wrapper"
echo once with a locally installed Gradle to regenerate it.
exit /b 1

:runWrapper
if defined JAVA_HOME (
  set JAVA_EXE=%JAVA_HOME%\bin\java.exe
) else (
  set JAVA_EXE=java.exe
)

"%JAVA_EXE%" -classpath "%APP_HOME%gradle\wrapper\gradle-wrapper.jar" org.gradle.wrapper.GradleWrapperMain %*
