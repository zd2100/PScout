@ECHO OFF
REM %1 = jar file path
REM %2 = output path

SET jarPath=%1
SET output=%2
SET winrar="C:\Program Files\WinRAR\WinRar.exe"

mkdir %output%
pushd %output%

for /r %jarPath% %%i in (*.jar) do (
	ECHO Extracting %%i
	%winrar% x -y %%i %2
)

popd