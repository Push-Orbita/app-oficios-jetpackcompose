# Helpers compartidos para E2E en emulador/dispositivo vía adb + uiautomator dump.
# Uso: . "$PSScriptRoot\_adb-ui-helpers.ps1"; Initialize-OficiosE2E -Serial $Serial

$script:OficiosE2E_Adb = $null
$script:OficiosE2E_Serial = $null
$script:OficiosE2E_Package = 'com.cocido.oficios'
$script:OficiosE2E_Apk = Join-Path $PSScriptRoot '..\..\app\build\outputs\apk\debug\app-debug.apk'

function Initialize-OficiosE2E {
    param(
        [Parameter(Mandatory = $true)][string]$Serial,
        [string]$Package = $script:OficiosE2E_Package,
        [string]$ApkPath = $script:OficiosE2E_Apk
    )
    $script:OficiosE2E_Serial = $Serial
    $script:OficiosE2E_Package = $Package
    $script:OficiosE2E_Apk = $ApkPath
    $script:OficiosE2E_Adb = Join-Path $env:LOCALAPPDATA 'Android\Sdk\platform-tools\adb.exe'
    if (-not (Test-Path $script:OficiosE2E_Adb)) {
        throw "No se encontró adb en $script:OficiosE2E_Adb. Instalá Android SDK platform-tools."
    }
}

function Invoke-OficiosAdb {
    param([Parameter(Mandatory = $true)][string[]]$Arguments)
    $output = & $script:OficiosE2E_Adb -s $script:OficiosE2E_Serial @Arguments
    if ($LASTEXITCODE -ne 0) { throw "adb falló: $($Arguments -join ' ')" }
    return $output
}

function Get-OficiosUiHierarchy {
    for ($attempt = 0; $attempt -lt 10; $attempt++) {
        & $script:OficiosE2E_Adb -s $script:OficiosE2E_Serial shell uiautomator dump /sdcard/oficios-e2e.xml 2>$null | Out-Null
        if ($LASTEXITCODE -eq 0) {
            $raw = Invoke-OficiosAdb @('exec-out', 'cat', '/sdcard/oficios-e2e.xml') | Out-String
            if ($raw.TrimStart().StartsWith('<?xml')) { return [xml]$raw }
        }
        Start-Sleep -Milliseconds 500
    }
    throw 'No se pudo obtener la jerarquía accesible del emulador.'
}

function Get-OficiosNodeCenter($Node) {
    if ($Node.bounds -notmatch '\[(\d+),(\d+)\]\[(\d+),(\d+)\]') { throw 'Nodo sin bounds válidos.' }
    $left = [int]$Matches[1]
    $top = [int]$Matches[2]
    $right = [int]$Matches[3]
    $bottom = [int]$Matches[4]
    return @([int](($left + $right) / 2), [int](($top + $bottom) / 2))
}

function Invoke-OficiosTapNode($Node) {
    $center = Get-OficiosNodeCenter $Node
    Invoke-OficiosAdb @('shell', 'input', 'tap', "$($center[0])", "$($center[1])") | Out-Null
    Start-Sleep -Milliseconds 350
}

function Find-OficiosText {
    param(
        [Parameter(Mandatory = $true)][string]$Text,
        [switch]$Contains
    )
    $xml = Get-OficiosUiHierarchy
    $nodes = if ($Contains) {
        $xml.SelectNodes("//node[contains(@text, '$Text')]")
    } else {
        $xml.SelectNodes("//node[@text='$Text']")
    }
    return $nodes | Select-Object -First 1
}

function Invoke-OficiosTapText {
    param([Parameter(Mandatory = $true)][string]$Text)
    $node = Find-OficiosText $Text
    if ($null -eq $node) { throw "No se encontró el texto: $Text" }
    $target = $node
    while ($null -ne $target.ParentNode -and $target.clickable -ne 'true') { $target = $target.ParentNode }
    Invoke-OficiosTapNode $target
}

function Invoke-OficiosTapDescription {
    param([Parameter(Mandatory = $true)][string]$Description)
    $node = (Get-OficiosUiHierarchy).SelectNodes("//node[@content-desc='$Description']") | Select-Object -First 1
    if ($null -eq $node) { throw "No se encontró la descripción accesible: $Description" }
    $target = $node
    while ($null -ne $target.ParentNode -and $target.clickable -ne 'true') { $target = $target.ParentNode }
    Invoke-OficiosTapNode $target
}

function Invoke-OficiosScrollToText {
    param([Parameter(Mandatory = $true)][string]$Text, [int]$MaxAttempts = 8)
    for ($attempt = 0; $attempt -lt $MaxAttempts; $attempt++) {
        if ($null -ne (Find-OficiosText $Text)) { return }
        Invoke-OficiosAdb @('shell', 'input', 'swipe', '1300', '1300', '1300', '450', '350') | Out-Null
        Start-Sleep -Milliseconds 400
    }
    throw "No se encontró luego de desplazar: $Text"
}

function Invoke-OficiosTapEditText {
    param([Parameter(Mandatory = $true)][int]$Index)
    $node = (Get-OficiosUiHierarchy).SelectNodes("//node[@class='android.widget.EditText']")[$Index]
    if ($null -eq $node) { throw "No se encontró el campo de texto $Index." }
    Invoke-OficiosTapNode $node
}

function Invoke-OficiosEnterText {
    param([Parameter(Mandatory = $true)][string]$Value)
    # Escapar espacios para adb input text
    $escaped = $Value -replace ' ', '%s'
    Invoke-OficiosAdb @('shell', 'input', 'text', $escaped) | Out-Null
    Start-Sleep -Milliseconds 250
}

function Wait-OficiosText {
    param(
        [Parameter(Mandatory = $true)][string]$Text,
        [int]$Seconds = 15,
        [switch]$Contains
    )
    for ($i = 0; $i -lt ($Seconds * 2); $i++) {
        if ($null -ne (Find-OficiosText $Text -Contains:$Contains)) { return }
        Start-Sleep -Milliseconds 500
    }
    throw "Tiempo agotado esperando: $Text"
}

function Install-OficiosDebugApk {
    param([switch]$SkipInstall)
    if ($SkipInstall) { return }
    if (-not (Test-Path $script:OficiosE2E_Apk)) {
        throw "APK no encontrado en $($script:OficiosE2E_Apk). Ejecutá: .\gradlew.bat :app:assembleDebug"
    }
    Invoke-OficiosAdb @('install', '-r', $script:OficiosE2E_Apk) | Out-Null
}

function Start-OficiosApp {
    Invoke-OficiosAdb @('shell', 'pm', 'clear', $script:OficiosE2E_Package) | Out-Null
    Invoke-OficiosAdb @('shell', 'am', 'start', '-n', "$($script:OficiosE2E_Package)/.MainActivity") | Out-Null
}

function Invoke-OficiosLogin {
    param(
        [Parameter(Mandatory = $true)][string]$Email,
        [Parameter(Mandatory = $true)][string]$Password
    )
    Wait-OficiosText 'Ingrese sus datos' -Contains
    Invoke-OficiosTapEditText 0
    Invoke-OficiosEnterText $Email
    Invoke-OficiosAdb @('shell', 'input', 'keyevent', 'KEYCODE_TAB') | Out-Null
    Start-Sleep -Milliseconds 250
    Invoke-OficiosEnterText $Password
    Invoke-OficiosAdb @('shell', 'input', 'keyevent', 'KEYCODE_BACK') | Out-Null
    Start-Sleep -Milliseconds 500
    Invoke-OficiosTapText 'Ingresar'
}

function Grant-OficiosRuntimePermissions {
    $permissions = @(
        'android.permission.ACCESS_FINE_LOCATION',
        'android.permission.ACCESS_COARSE_LOCATION',
        'android.permission.POST_NOTIFICATIONS',
        'android.permission.CAMERA'
    )
    foreach ($permission in $permissions) {
        Invoke-OficiosAdb @('shell', 'pm', 'grant', $script:OficiosE2E_Package, $permission) 2>$null | Out-Null
    }
}
