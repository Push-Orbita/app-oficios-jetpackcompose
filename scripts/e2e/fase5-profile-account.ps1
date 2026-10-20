param(
    [Parameter(Mandatory = $true)][string]$Serial,
    [Parameter(Mandatory = $true)][string]$Email,
    [Parameter(Mandatory = $true)][string]$Password,
    [switch]$SkipInstall
)

$ErrorActionPreference = 'Stop'
$adb = Join-Path $env:LOCALAPPDATA 'Android\Sdk\platform-tools\adb.exe'
$apk = Join-Path $PSScriptRoot '..\..\app\build\outputs\apk\debug\app-debug.apk'
$package = 'com.cocido.oficios'

function Invoke-Adb([string[]]$Arguments) {
    $output = & $adb -s $Serial @Arguments
    if ($LASTEXITCODE -ne 0) { throw "adb falló: $($Arguments -join ' ')" }
    return $output
}

function Get-Hierarchy {
    for ($attempt = 0; $attempt -lt 10; $attempt++) {
        & $adb -s $Serial shell uiautomator dump /sdcard/oficios-e2e.xml 2>$null | Out-Null
        if ($LASTEXITCODE -eq 0) {
            $raw = Invoke-Adb @('exec-out', 'cat', '/sdcard/oficios-e2e.xml') | Out-String
            if ($raw.TrimStart().StartsWith('<?xml')) { return [xml]$raw }
        }
        Start-Sleep -Milliseconds 500
    }
    throw 'No se pudo obtener la jerarquía accesible del emulador.'
}

function Get-Center($Node) {
    if ($Node.bounds -notmatch '\[(\d+),(\d+)\]\[(\d+),(\d+)\]') { throw 'Nodo sin bounds válidos.' }
    $left = [int]$Matches[1]
    $top = [int]$Matches[2]
    $right = [int]$Matches[3]
    $bottom = [int]$Matches[4]
    return @([int](($left + $right) / 2), [int](($top + $bottom) / 2))
}

function Tap-Node($Node) {
    $center = Get-Center $Node
    Invoke-Adb @('shell', 'input', 'tap', "$($center[0])", "$($center[1])") | Out-Null
    Start-Sleep -Milliseconds 350
}

function Find-Text([string]$Text, [switch]$Contains) {
    $xml = Get-Hierarchy
    $nodes = if ($Contains) {
        $xml.SelectNodes("//node[contains(@text, '$Text')]")
    } else {
        $xml.SelectNodes("//node[@text='$Text']")
    }
    return $nodes | Select-Object -First 1
}

function Tap-Text([string]$Text) {
    $node = Find-Text $Text
    if ($null -eq $node) { throw "No se encontró el texto: $Text" }
    $target = $node
    while ($null -ne $target.ParentNode -and $target.clickable -ne 'true') { $target = $target.ParentNode }
    Tap-Node $target
}

function Tap-Description([string]$Description) {
    $node = (Get-Hierarchy).SelectNodes("//node[@content-desc='$Description']") | Select-Object -First 1
    if ($null -eq $node) { throw "No se encontró la descripción accesible: $Description" }
    $target = $node
    while ($null -ne $target.ParentNode -and $target.clickable -ne 'true') { $target = $target.ParentNode }
    Tap-Node $target
}

function Scroll-ToText([string]$Text) {
    for ($attempt = 0; $attempt -lt 8; $attempt++) {
        if ($null -ne (Find-Text $Text)) { return }
        Invoke-Adb @('shell', 'input', 'swipe', '1300', '1300', '1300', '450', '350') | Out-Null
        Start-Sleep -Milliseconds 400
    }
    throw "No se encontró luego de desplazar: $Text"
}

function Tap-EditText([int]$Index) {
    $node = (Get-Hierarchy).SelectNodes("//node[@class='android.widget.EditText']")[$Index]
    if ($null -eq $node) { throw "No se encontró el campo de texto $Index." }
    Tap-Node $node
}

function Enter-Text([string]$Value) {
    Invoke-Adb @('shell', 'input', 'text', $Value) | Out-Null
    Start-Sleep -Milliseconds 250
}

function Wait-Text([string]$Text, [int]$Seconds = 15) {
    for ($i = 0; $i -lt ($Seconds * 2); $i++) {
        if ($null -ne (Find-Text $Text -Contains)) { return }
        Start-Sleep -Milliseconds 500
    }
    throw "Tiempo agotado esperando: $Text"
}

if (-not $SkipInstall) {
    Invoke-Adb @('install', '-r', $apk) | Out-Null
}
Invoke-Adb @('shell', 'pm', 'clear', $package) | Out-Null
Invoke-Adb @('shell', 'am', 'start', '-n', "$package/.MainActivity") | Out-Null
Wait-Text 'Ingrese sus datos'

Tap-EditText 0
Enter-Text $Email
Invoke-Adb @('shell', 'input', 'keyevent', 'KEYCODE_TAB') | Out-Null
Start-Sleep -Milliseconds 250
Enter-Text $Password
Invoke-Adb @('shell', 'input', 'keyevent', 'KEYCODE_BACK') | Out-Null
Start-Sleep -Milliseconds 500
Tap-Text 'Ingresar'

Wait-Text 'Inicio' 20
Tap-Text 'Ajustes'
Wait-Text 'Mi perfil'

$expected = @('Datos personales', 'Presentación profesional', 'Servicios que ofrecés', 'Galería y certificados', 'Cambiar contraseña')
for ($attempt = 0; $attempt -lt 6; $attempt++) {
    $xml = Get-Hierarchy
    $visible = @($xml.SelectNodes('//node[@text!=""]') | ForEach-Object { $_.text })
    $missing = @($expected | Where-Object { $_ -notin $visible })
    if ($missing.Count -eq 0) { break }
    Invoke-Adb @('shell', 'input', 'swipe', '1300', '1300', '1300', '450', '350') | Out-Null
    Start-Sleep -Milliseconds 400
}
if ($missing.Count -gt 0) { throw "Secciones no encontradas: $($missing -join ', ')" }

Scroll-ToText 'Datos personales'
Tap-Text 'Datos personales'
Wait-Text 'Nombre'
Tap-Description 'Volver'
Wait-Text 'Mi perfil'

Scroll-ToText 'Presentación profesional'
Tap-Text 'Presentación profesional'
Wait-Text 'Descripción profesional'
Tap-Description 'Volver'
Wait-Text 'Mi perfil'

Scroll-ToText 'Servicios que ofrecés'
Tap-Text 'Servicios que ofrecés'
Wait-Text 'Cada servicio agrupa'
Wait-Text 'Agregar otro servicio'
Tap-Description 'Volver'
Wait-Text 'Mi perfil'

Scroll-ToText 'Galería y certificados'
Tap-Text 'Galería y certificados'
Wait-Text 'Usá fotos reales'
Tap-Description 'Volver'
Wait-Text 'Mi perfil'

Write-Output 'FASE5_E2E_OK: login, Home, cuenta, datos personales, perfil profesional, servicios y portfolio verificados.'
