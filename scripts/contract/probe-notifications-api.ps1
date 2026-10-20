param(
    [Parameter(Mandatory = $true)][string]$Email,
    [Parameter(Mandatory = $true)][string]$Password,
    [string]$BaseUrl = 'https://apioficios.pushsoftware.com.ar/',
    [string]$OutputPath = (Join-Path $PSScriptRoot '..\..\docs\backend\samples\notifications-response.json'),
    [switch]$SoloNoLeidas
)

$ErrorActionPreference = 'Stop'
$loginBody = @{ email = $Email; contrasena = $Password } | ConvertTo-Json
$login = Invoke-RestMethod -Method Post -Uri ($BaseUrl + 'auth/login') -ContentType 'application/json' -Body $loginBody
$token = $login.access_token
if ([string]::IsNullOrWhiteSpace($token)) { throw 'Login sin access_token.' }

$uri = $BaseUrl + 'notifications?pageSize=20&pageNumber=1'
if ($SoloNoLeidas) { $uri += '&soloNoLeidas=true' }

$response = Invoke-RestMethod -Method Get -Uri $uri -Headers @{ Authorization = "Bearer $token" }
$outDir = Split-Path $OutputPath -Parent
if (-not (Test-Path $outDir)) { New-Item -ItemType Directory -Path $outDir -Force | Out-Null }
$response | ConvertTo-Json -Depth 20 | Set-Content -Path $OutputPath -Encoding UTF8

Write-Host "Respuesta guardada en $OutputPath" -ForegroundColor Green
Write-Host ($response | ConvertTo-Json -Depth 5 -Compress)
