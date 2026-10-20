<#
.SYNOPSIS
    Ejecuta connectedAndroidTest en módulos feature con el AVD recomendado.

.NOTES
    Evitar Pixel_9_Pro / API 37 preview — rompe Espresso (InputManager).
    Usar Pixel_Tablet API 35.
#>
param(
    [string]$Serial,
    [string[]]$Modules = @(
        ':feature:auth',
        ':feature:home',
        ':feature:onboarding',
        ':feature:settings',
        ':core:designsystem'
    )
)

$ErrorActionPreference = 'Stop'
$root = Join-Path $PSScriptRoot '..\..'
Push-Location $root
try {
    if ($Serial) {
        $env:ANDROID_SERIAL = $Serial
        Write-Host "ANDROID_SERIAL=$Serial" -ForegroundColor Cyan
    }

    $tasks = $Modules | ForEach-Object { "$($_):connectedDebugAndroidTest" }
    Write-Host "Ejecutando: $($tasks -join ' ')" -ForegroundColor Cyan
    & .\gradlew.bat @tasks
    if ($LASTEXITCODE -ne 0) { throw "connectedAndroidTest falló con código $LASTEXITCODE" }
    Write-Host "Tests instrumentados OK." -ForegroundColor Green
} finally {
    Pop-Location
}
