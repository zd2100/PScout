@ECHO OFF
REM %1 = jar file path
REM %2 = output path

SET jarPath=%1
SET output=%2
SET jdkPath="C:\Program Files\Java\jdk1.8.0_66\bin\jar"

mkdir %output%
pushd %output%

for /r %jarPath% %%i in (*.jar) do (
	ECHO Extracting %%i
	%jdkPath% xf %%i
)

popd