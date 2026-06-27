$commands = @(
    'open youtube', 'search youtube', 'open settings', 'flashlight', 'bluetooth', 
    'wifi', 'hotspot', 'volume', 'mute', 'brightness', 'clipboard', 'battery', 
    'storage', 'ram', 'call', 'sms', 'whatsapp', 'contact', 'photo', 'record video', 
    'voice note', 'calendar', 'add calendar event', 'alarm', 'timer', 'stopwatch', 
    'search web', 'browser'
)

foreach ($cmd in $commands) {
    & "C:\Users\apple\AppData\Local\Android\Sdk\platform-tools\adb.exe" logcat -c
    & "C:\Users\apple\AppData\Local\Android\Sdk\platform-tools\adb.exe" shell am force-stop com.example.emeth
    & "C:\Users\apple\AppData\Local\Android\Sdk\platform-tools\adb.exe" shell am start -n com.example.emeth/com.emeth.kernel.MainActivity --es command `"`"$cmd`"`" | Out-Null
    Start-Sleep -s 1
    $log = & "C:\Users\apple\AppData\Local\Android\Sdk\platform-tools\adb.exe" logcat -d | findstr Emeth
    Write-Host "CMD: $cmd -> LOG: $log"
}
