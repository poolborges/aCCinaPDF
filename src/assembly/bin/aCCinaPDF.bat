@ECHO OFF
CLS

SET JAR_NAME=aCCinaPDF.jar

FOR /f %%j IN ("java.exe") DO (
	SET JAVA_HOME=%%~dp$PATH:j
)

IF %JAVA_HOME%.==. (
	START "" https://www.java.com/download/
	@ECHO Could not find a working Java Virtual Machine.
	@ECHO Please install Java on your computer to run this application.
	PAUSE
) ELSE (
	IF EXIST %JAR_NAME% (
		@ECHO Launching aCCinaPDF...
		START /MIN %JAVA_HOME%/java -jar %JAR_NAME%
		PING 1.1.1.1 -n 1 -w 2500 > NUL
	) ELSE (
		@ECHO Failed to start the application.
		@ECHO Could not locate "%JAR_NAME%" on your current directory.
		PAUSE
	)
)