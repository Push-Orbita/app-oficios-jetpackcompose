<#
.SYNOPSIS
    E2E pedidos Fase 8 — requiere credenciales staging y gates backend (Twilio, agenda, orders).

.DESCRIPTION
    Flujo objetivo:
    1. Login cliente → crear solicitud con slotTurnoId
    2. Login trabajador → aceptar solicitud
    3. Tracking → finalizar con código → calificar

    Hoy puede fallar en pasos bloqueados por backend; el script documenta el estado de cada gate.

.EXAMPLE
    .\scripts\e2e\fase8-pedidos-e2e.ps1 -ClientEmail x@y.com -ClientPassword s -WorkerEmail a@b.com -WorkerPassword s
#>
param(
    [Parameter(Mandatory = $true)][string]$ClientEmail,
    [Parameter(Mandatory = $true)][string]$ClientPassword,
    [Parameter(Mandatory = $true)][string]$WorkerEmail,
    [Parameter(Mandatory = $true)][string]$WorkerPassword,
    [string]$BaseUrl = 'https://apioficios.pushsoftware.com.ar/'
)

$ErrorActionPreference = 'Stop'

function Get-AuthToken {
    param([string]$Email, [string]$Password)
    $body = @{ email = $Email; contrasena = $Password } | ConvertTo-Json
    $login = Invoke-RestMethod -Method Post -Uri ($BaseUrl + 'auth/login') -ContentType 'application/json' -Body $body
    if ([string]::IsNullOrWhiteSpace($login.access_token)) { throw "Login falló para $Email" }
    return $login.access_token
}

Write-Host "== Fase 8 E2E pedidos (API) ==" -ForegroundColor Cyan
Write-Host "Verificando gates con verify-fase8-backend.ps1..." -ForegroundColor DarkGray
& "$PSScriptRoot\..\contract\verify-fase8-backend.ps1"

$clientToken = Get-AuthToken -Email $ClientEmail -Password $ClientPassword
$workerToken = Get-AuthToken -Email $WorkerEmail -Password $WorkerPassword

Write-Host "[OK] Login cliente y trabajador" -ForegroundColor Green
Write-Host "[INFO] Creación de solicitud con slot requiere IDs reales (trabajador, domicilio, contacto, slot)." -ForegroundColor Yellow
Write-Host "[INFO] Completar manualmente cuando Twilio OTP y BACK-AGENDA-002 estén cerrados." -ForegroundColor Yellow
Write-Host "`nScript E2E Fase 8 preparado. Extender con IDs de staging cuando el equipo los provea." -ForegroundColor Cyan
