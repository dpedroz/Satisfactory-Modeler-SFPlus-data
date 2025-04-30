@echo off
setlocal enabledelayedexpansion

:: --- USER CONFIGURATION ---
:: IMPORTANT: SET THE FULL PATH TO YOUR TARGET FOLDER HERE
:: Example: set TARGET_DIR=s:\SteamLibrary\steamapps\common\Satisfactory Modeler
set TARGET_DIR=C:\Path\To\Your\TARGET\Folder
:: --- END USER CONFIGURATION ---

:: Validate TARGET_DIR
if "%TARGET_DIR%"=="C:\Path\To\Your\TARGET\Folder" (
    echo ERROR: Please edit this script (%~nx0^) and set the TARGET_DIR variable correctly.
    goto :End
)
if not exist "%TARGET_DIR%\" (
    echo ERROR: The configured TARGET_DIR does not exist:
    echo        "%TARGET_DIR%"
    echo       Please create it or correct the path in the script (%~nx0^).
    goto :End
)

:: Get the directory where this script is located (which is the Project Dir)
set PROJECT_DIR=%~dp0
:: Remove trailing backslash if present for robocopy compatibility
if "%PROJECT_DIR:~-1%"=="\" set PROJECT_DIR=%PROJECT_DIR:~0,-1%
:: --- Define the folders to sync ---
set SYNC_FOLDERS=game_data images

echo "Proceeding with mirror operation (Project -> Target)..."
for %%F in (%SYNC_FOLDERS%) do (
    set "PROJECT_SUBDIR=%PROJECT_DIR%\%%F"
    set "TARGET_SUBDIR=%TARGET_DIR%\%%F"

    if exist "!PROJECT_SUBDIR!\" (
        echo == Mirroring Folder: %%F ==
        rem /MIR = Mirror (equivalent to /E /PURGE)
        rem /FFT = Use 2-sec timestamp compare (consistent with check)
        rem /R:2 /W:5 = Retry twice, wait 5s between retries (sensible defaults)
        rem /NJH /NJS /NP /NDL = Reduce noise during actual copy
        robocopy "!PROJECT_SUBDIR!" "!TARGET_SUBDIR!" /MIR /FFT /R:2 /W:5 /NJH /NJS /NP /NDL
        echo.
    ) else (
        echo "-- Skipping Project folder %%F (Not found for deploy) --"
        echo.
    )
)
goto :End

:End
echo.
exit /b