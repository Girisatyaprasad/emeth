$adb = "C:\Users\apple\AppData\Local\Android\Sdk\platform-tools\adb.exe"

& $adb logcat -c

# Test 1
& $adb shell am force-stop com.example.emeth
& $adb shell am start -n com.example.emeth/com.emeth.kernel.MainActivity --es command \`"open youtube\`"
Start-Sleep -s 3
& $adb shell input keyevent 3 # HOME

# Test 2
& $adb shell am force-stop com.example.emeth
& $adb shell am start -n com.example.emeth/com.emeth.kernel.MainActivity --es command \`"open settings\`"
Start-Sleep -s 3
& $adb shell input keyevent 3

# Test 3
& $adb shell am force-stop com.example.emeth
& $adb shell am start -n com.example.emeth/com.emeth.kernel.MainActivity --es command \`"what battery do i have\`"
Start-Sleep -s 3

# Test 4
& $adb shell am force-stop com.example.emeth
& $adb shell pm grant com.example.emeth android.permission.READ_CONTACTS
& $adb shell am start -n com.example.emeth/com.emeth.kernel.MainActivity --es command \`"who are my contacts named mom\`"
Start-Sleep -s 3

# Test 5
& $adb shell am force-stop com.example.emeth
& $adb shell pm revoke com.example.emeth android.permission.READ_CONTACTS
& $adb shell am start -n com.example.emeth/com.emeth.kernel.MainActivity --es command \`"who are my contacts named mom\`"
Start-Sleep -s 3

# Test 7
& $adb shell am force-stop com.example.emeth
& $adb shell am start -n com.example.emeth/com.emeth.kernel.MainActivity --es command \`"open chrome\`"
Start-Sleep -s 3
& $adb shell input keyevent 3

# Test 8
& $adb shell am force-stop com.example.emeth
& $adb shell am start -n com.example.emeth/com.emeth.kernel.MainActivity --es command \`"open calendar\`"
Start-Sleep -s 3
& $adb shell input keyevent 3

# Test 9
& $adb shell am force-stop com.example.emeth
& $adb shell am start -n com.example.emeth/com.emeth.kernel.MainActivity --es command \`"set alarm for 7am\`"
Start-Sleep -s 3
& $adb shell input keyevent 3

& $adb logcat -d | Select-String "Emeth"
