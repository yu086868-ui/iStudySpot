$ErrorActionPreference = "SilentlyContinue"

Write-Host "=== iStudySpot UI Test ===" -ForegroundColor Yellow

adb shell monkey -p com.example.scyiler.istudyspot -c android.intent.category.LAUNCHER 1 | Out-Null
Start-Sleep -Seconds 4
adb shell input tap 135 2270 | Out-Null
Start-Sleep -Seconds 2

Write-Host "[TEST 1] StudyRoom List" -ForegroundColor Green
adb logcat -c | Out-Null
adb shell input tap 135 875 | Out-Null
Start-Sleep -Seconds 3
$sr = adb logcat -d -t 50 | Select-String "studyrooms" | Select-String "200"
if ($sr) { Write-Host "  PASS" -ForegroundColor Green } else { Write-Host "  FAIL" -ForegroundColor Red }

Write-Host "[TEST 2] Seat Map" -ForegroundColor Green
adb shell input tap 540 600 | Out-Null
Start-Sleep -Seconds 3
$seat = adb logcat -d -t 50 | Select-String "seats" | Select-String "200"
if ($seat) { Write-Host "  PASS" -ForegroundColor Green } else { Write-Host "  FAIL" -ForegroundColor Red }

Write-Host "[TEST 3] Login" -ForegroundColor Green
adb shell input keyevent 4 | Out-Null
Start-Sleep -Seconds 1
adb shell input tap 135 2270 | Out-Null
Start-Sleep -Seconds 1
adb shell input tap 945 2270 | Out-Null
Start-Sleep -Seconds 2
adb shell input tap 540 900 | Out-Null
Start-Sleep -Seconds 2
adb shell input tap 540 700 | Out-Null
Start-Sleep -Milliseconds 500
adb shell input text testuser | Out-Null
Start-Sleep -Milliseconds 500
adb shell input tap 540 850 | Out-Null
Start-Sleep -Milliseconds 500
adb shell input text 123456 | Out-Null
Start-Sleep -Milliseconds 500
adb logcat -c | Out-Null
adb shell input tap 540 1000 | Out-Null
Start-Sleep -Seconds 3
$login = adb logcat -d -t 50 | Select-String "auth/login" | Select-String "200"
if ($login) { Write-Host "  PASS" -ForegroundColor Green } else { Write-Host "  FAIL" -ForegroundColor Red }

Write-Host "[TEST 4] Booking" -ForegroundColor Green
adb shell input keyevent 4 | Out-Null
Start-Sleep -Seconds 1
adb shell input tap 135 2270 | Out-Null
Start-Sleep -Seconds 1
adb logcat -c | Out-Null
adb shell input tap 135 875 | Out-Null
Start-Sleep -Seconds 3
adb shell input tap 540 600 | Out-Null
Start-Sleep -Seconds 3
adb shell input tap 300 800 | Out-Null
Start-Sleep -Seconds 2
adb shell input tap 540 2100 | Out-Null
Start-Sleep -Seconds 3
$book = adb logcat -d -t 50 | Select-String "reservations" | Select-String "200"
if ($book) { Write-Host "  PASS" -ForegroundColor Green } else { Write-Host "  FAIL" -ForegroundColor Red }

Write-Host "[TEST 5] AI Chat" -ForegroundColor Green
adb shell input keyevent 4 | Out-Null
Start-Sleep -Seconds 1
adb shell input tap 135 2270 | Out-Null
Start-Sleep -Seconds 1
adb logcat -c | Out-Null
adb shell input tap 405 1078 | Out-Null
Start-Sleep -Seconds 3
adb shell input tap 540 600 | Out-Null
Start-Sleep -Seconds 2
adb shell input tap 540 2200 | Out-Null
Start-Sleep -Milliseconds 500
adb shell input text hello | Out-Null
adb shell input keyevent 66 | Out-Null
Start-Sleep -Seconds 3
$ai = adb logcat -d -t 50 | Select-String "chat"
if ($ai) { Write-Host "  PASS" -ForegroundColor Green } else { Write-Host "  FAIL" -ForegroundColor Red }

Write-Host "[TEST 6] Rules" -ForegroundColor Green
adb shell input keyevent 4 | Out-Null
Start-Sleep -Seconds 1
adb shell input tap 135 2270 | Out-Null
Start-Sleep -Seconds 1
adb logcat -c | Out-Null
adb shell input tap 405 2270 | Out-Null
Start-Sleep -Seconds 3
$rules = adb logcat -d -t 50 | Select-String "rules" | Select-String "200"
if ($rules) { Write-Host "  PASS" -ForegroundColor Green } else { Write-Host "  FAIL" -ForegroundColor Red }

Write-Host "=== Test Complete ===" -ForegroundColor Yellow
