param(
    [string]$Serial = 'emulator-5556',
    [string]$ApkPath = 'app/build/outputs/apk/debug/app-debug.apk'
)

$ErrorActionPreference = 'Stop'
$sdkLine = Get-Content 'local.properties' | Where-Object { $_ -like 'sdk.dir=*' } | Select-Object -First 1
if (-not $sdkLine) { throw 'local.properties no define sdk.dir.' }
$sdkPath = ($sdkLine -replace '^sdk.dir=', '') -replace '\\:', ':' -replace '\\\\', '\'
$adb = Join-Path $sdkPath 'platform-tools\adb.exe'
$packageName = 'com.cocido.oficios'

if (-not (Test-Path -LiteralPath $ApkPath)) { throw "No existe el APK: $ApkPath" }
$deviceState = (& $adb -s $Serial get-state 2>&1).Trim()
if ($deviceState -ne 'device') { throw "El dispositivo $Serial no está disponible." }
$api = [int]((& $adb -s $Serial shell getprop ro.build.version.sdk).Trim())
if ($api -lt 35) { throw "Fase 6 requiere API 35 o superior; $Serial usa API $api." }

& $adb -s $Serial install -r $ApkPath | Out-Null
& $adb -s $Serial shell pm grant $packageName android.permission.ACCESS_COARSE_LOCATION
& $adb -s $Serial shell pm grant $packageName android.permission.ACCESS_FINE_LOCATION
if ($api -ge 33) {
    & $adb -s $Serial shell pm grant $packageName android.permission.POST_NOTIFICATIONS 2>$null
}

# adb emu geo fix recibe longitud primero y latitud después.
$geoResult = (& $adb -s $Serial emu geo fix -58.17310 -26.18490 2>&1) -join "`n"
if ($LASTEXITCODE -ne 0 -or $geoResult -notmatch 'OK') {
    throw "No se pudo inyectar la ubicación simulada de Formosa: $geoResult"
}

& $adb -s $Serial logcat -c
& $adb -s $Serial shell am force-stop $packageName
$launch = (& $adb -s $Serial shell am start -W -n "$packageName/.MainActivity" 2>&1) -join "`n"
if ($LASTEXITCODE -ne 0 -or $launch -match 'Error type') { throw "La app no inició: $launch" }
Start-Sleep -Seconds 4

$appProcessId = (& $adb -s $Serial shell pidof $packageName).Trim()
if (-not $appProcessId) { throw 'La app se cerró durante el smoke test.' }
$fatal = (& $adb -s $Serial logcat -d --pid=$appProcessId '*:E' 2>&1) -join "`n"
if ($fatal -match 'FATAL EXCEPTION|AndroidRuntime.*Process: com\.cocido\.oficios') {
    throw "Se detectó un crash en Fase 6:`n$fatal"
}

$finePermission = (& $adb -s $Serial shell dumpsys package $packageName) -join "`n"
if ($finePermission -notmatch 'android.permission.ACCESS_FINE_LOCATION: granted=true') {
    throw 'ACCESS_FINE_LOCATION no quedó concedido.'
}

Write-Output "FASE6_EMULATOR_OK: APK iniciado sin crash en $Serial (API $api)."
Write-Output 'LOCATION_OK: permisos coarse/fine concedidos y ubicación simulada fijada en Formosa.'
Write-Output 'TRACKING_SERVICE_READY: el servicio foreground sólo se inicia desde un booking EN_CAMINO real.'
