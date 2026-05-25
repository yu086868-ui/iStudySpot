@echo off
echo === iStudySpot UI Test ===

echo [PREP] Starting app...
adb shell monkey -p com.example.scyiler.istudyspot -c android.intent.category.LAUNCHER 1 >nul 2>&1
timeout /t 4 /nobreak >nul
adb shell input tap 135 2270 >nul 2>&1
timeout /t 2 /nobreak >nul

echo [TEST 1] StudyRoom List
adb logcat -c >nul 2>&1
adb shell input tap 135 875 >nul 2>&1
timeout /t 3 /nobreak >nul
adb logcat -d -t 50 2>nul | findstr "studyrooms" | findstr "200" >nul
if %ERRORLEVEL%==0 (echo   PASS) else (echo   FAIL)

echo [TEST 2] Seat Map
adb shell input tap 540 600 >nul 2>&1
timeout /t 3 /nobreak >nul
adb logcat -d -t 50 2>nul | findstr "seats" | findstr "200" >nul
if %ERRORLEVEL%==0 (echo   PASS) else (echo   FAIL)

echo [TEST 3] Login
adb shell input keyevent 4 >nul 2>&1
timeout /t 1 /nobreak >nul
adb shell input tap 135 2270 >nul 2>&1
timeout /t 1 /nobreak >nul
adb shell input tap 945 2270 >nul 2>&1
timeout /t 2 /nobreak >nul
adb shell input tap 540 900 >nul 2>&1
timeout /t 2 /nobreak >nul
adb shell input tap 540 700 >nul 2>&1
timeout /t 1 /nobreak >nul
adb shell input text testuser >nul 2>&1
timeout /t 1 /nobreak >nul
adb shell input tap 540 850 >nul 2>&1
timeout /t 1 /nobreak >nul
adb shell input text 123456 >nul 2>&1
timeout /t 1 /nobreak >nul
adb logcat -c >nul 2>&1
adb shell input tap 540 1000 >nul 2>&1
timeout /t 3 /nobreak >nul
adb logcat -d -t 50 2>nul | findstr "auth/login" | findstr "200" >nul
if %ERRORLEVEL%==0 (echo   PASS) else (echo   FAIL)

echo [TEST 4] Booking
adb shell input keyevent 4 >nul 2>&1
timeout /t 1 /nobreak >nul
adb shell input tap 135 2270 >nul 2>&1
timeout /t 1 /nobreak >nul
adb logcat -c >nul 2>&1
adb shell input tap 135 875 >nul 2>&1
timeout /t 3 /nobreak >nul
adb shell input tap 540 600 >nul 2>&1
timeout /t 3 /nobreak >nul
adb shell input tap 300 800 >nul 2>&1
timeout /t 2 /nobreak >nul
adb shell input tap 540 2100 >nul 2>&1
timeout /t 3 /nobreak >nul
adb logcat -d -t 50 2>nul | findstr "reservations" | findstr "200" >nul
if %ERRORLEVEL%==0 (echo   PASS) else (echo   FAIL)

echo [TEST 5] AI Chat
adb shell input keyevent 4 >nul 2>&1
timeout /t 1 /nobreak >nul
adb shell input tap 135 2270 >nul 2>&1
timeout /t 1 /nobreak >nul
adb logcat -c >nul 2>&1
adb shell input tap 405 1078 >nul 2>&1
timeout /t 3 /nobreak >nul
adb shell input tap 540 600 >nul 2>&1
timeout /t 2 /nobreak >nul
adb shell input tap 540 2200 >nul 2>&1
timeout /t 1 /nobreak >nul
adb shell input text hello >nul 2>&1
adb shell input keyevent 66 >nul 2>&1
timeout /t 3 /nobreak >nul
adb logcat -d -t 50 2>nul | findstr "chat" >nul
if %ERRORLEVEL%==0 (echo   PASS) else (echo   FAIL)

echo [TEST 6] Rules
adb shell input keyevent 4 >nul 2>&1
timeout /t 1 /nobreak >nul
adb shell input tap 135 2270 >nul 2>&1
timeout /t 1 /nobreak >nul
adb logcat -c >nul 2>&1
adb shell input tap 405 2270 >nul 2>&1
timeout /t 3 /nobreak >nul
adb logcat -d -t 50 2>nul | findstr "rules" | findstr "200" >nul
if %ERRORLEVEL%==0 (echo   PASS) else (echo   FAIL)

echo === Test Complete ===
