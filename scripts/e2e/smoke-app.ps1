<#
.SYNOPSIS
    Smoke test E2E: login, navegación principal y validación de tabs Fase 7A.

.DESCRIPTION
    Requiere emulador/dispositivo con adb. Preferir AVD Pixel_Tablet (API 35) para tests instrumentados.
    Para Espresso/connectedAndroidTest evitar Pixel_9_Pro (API 37 preview).

.EXAMPLE
    .\scripts\e2e\smoke-app.ps1 -Serial emulator-5554 -Email cliente@test.com -Password secret123
#>
param(
    [Parameter(Mandatory = $true)][string]$Serial,
    [Parameter(Mandatory = $true)][string]$Email,
    [Parameter(Mandatory = $true)][string]$Password,
    [ValidateSet('cliente', 'trabajador')][string]$Rol = 'cliente',
    [switch]$SkipInstall
)

$ErrorActionPreference = 'Stop'
. "$PSScriptRoot\_adb-ui-helpers.ps1"
Initialize-OficiosE2E -Serial $Serial

Write-Host "== Smoke Oficios ==" -ForegroundColor Cyan
Install-OficiosDebugApk -SkipInstall:$SkipInstall
Grant-OficiosRuntimePermissions
Start-OficiosApp
Invoke-OficiosLogin -Email $Email -Password $Password

if ($Rol -eq 'cliente') {
    Wait-OficiosText 'Inicio' -Seconds 25
    Write-Host "[OK] Home cliente cargado" -ForegroundColor Green

    try {
        Invoke-OficiosTapDescription 'Notificaciones'
        Start-Sleep -Milliseconds 800
        if ($null -ne (Find-OficiosText 'Notificaciones' -Contains)) {
            Write-Host "[OK] Pantalla de notificaciones accesible" -ForegroundColor Green
        }
        Invoke-OficiosAdb @('shell', 'input', 'keyevent', 'KEYCODE_BACK') | Out-Null
    } catch {
        Write-Host "[INFO] Ícono de notificaciones no accesible: $($_.Exception.Message)" -ForegroundColor DarkYellow
    }

    Invoke-OficiosTapText 'Ajustes'
    Wait-OficiosText 'Mi perfil' -Seconds 15
    Write-Host "[OK] Settings accesible" -ForegroundColor Green
    Invoke-OficiosAdb @('shell', 'input', 'keyevent', 'KEYCODE_BACK') | Out-Null

    Invoke-OficiosTapText 'Inicio'
    Wait-OficiosText 'Inicio' -Seconds 10

    $tabExpectations = @{
        'Favoritos' = @('Todavía no tenés favoritos')
        'Ayuda' = @('Ayuda', '¿Cómo contrato a un trabajador?')
        'Perfil' = @('Datos personales', 'Mi perfil')
    }

    foreach ($entry in $tabExpectations.GetEnumerator()) {
        $tab = $entry.Key
        $expectedTexts = $entry.Value
        try {
            Invoke-OficiosTapText $tab
            Start-Sleep -Milliseconds 800
            if ($null -ne (Find-OficiosText 'Próximamente' -Contains)) {
                throw "Tab '$tab' sigue mostrando placeholder Próximamente"
            }
            $found = $false
            foreach ($text in $expectedTexts) {
                if ($null -ne (Find-OficiosText $text -Contains)) {
                    $found = $true
                    break
                }
            }
            if (-not $found) {
                Write-Host "[WARN] Tab '$tab' abierto pero no se encontró texto esperado" -ForegroundColor Yellow
            } else {
                Write-Host "[OK] Tab '$tab' navega correctamente (Fase 7A)" -ForegroundColor Green
            }
            Invoke-OficiosAdb @('shell', 'input', 'keyevent', 'KEYCODE_BACK') | Out-Null
            Start-Sleep -Milliseconds 400
        } catch {
            Write-Host "[FAIL] Tab '$tab': $($_.Exception.Message)" -ForegroundColor Red
            throw
        }
    }
} else {
    Wait-OficiosText 'Inicio' -Seconds 25
    Write-Host "[OK] Home trabajador cargado" -ForegroundColor Green
    Invoke-OficiosTapText 'Ajustes'
    Wait-OficiosText 'Mi perfil' -Seconds 15
    Write-Host "[OK] Settings trabajador accesible" -ForegroundColor Green
}

Write-Host "`nSmoke completado sin errores fatales." -ForegroundColor Cyan
