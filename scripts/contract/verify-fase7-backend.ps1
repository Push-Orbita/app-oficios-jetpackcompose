param([string]$OpenApiUrl = 'https://apioficios.pushsoftware.com.ar/api-json')

$ErrorActionPreference = 'Stop'
$spec = Invoke-RestMethod -Uri $OpenApiUrl

$requiredOperations = @(
    @('get', '/notifications'),
    @('post', '/notifications/device-token'),
    @('patch', '/notifications/{id}/read')
)

$missing = foreach ($operation in $requiredOperations) {
    $method = $operation[0]
    $path = $operation[1]
    if ($null -eq $spec.paths.$path -or $null -eq $spec.paths.$path.$method) {
        "$($method.ToUpper()) $path"
    }
}

if ($missing.Count -gt 0) {
    throw "Faltan operaciones de notificaciones: $($missing -join ', ')"
}

function Assert-SchemaExists {
    param([Parameter(Mandatory = $true)][string]$SchemaName)
    if ($null -eq $spec.components.schemas.$SchemaName) {
        throw "Swagger no publica el schema $SchemaName."
    }
}

function Assert-Properties {
    param(
        [Parameter(Mandatory = $true)][string]$SchemaName,
        [Parameter(Mandatory = $true)][string[]]$RequiredProperties
    )
    Assert-SchemaExists $SchemaName
    $schema = $spec.components.schemas.$SchemaName
    $missingProperties = $RequiredProperties | Where-Object {
        $_ -notin $schema.properties.PSObject.Properties.Name
    }
    if ($missingProperties.Count -gt 0) {
        throw "$SchemaName no publica: $($missingProperties -join ', ')"
    }
}

Assert-Properties 'RegisterDeviceTokenRequestDto' @('fcmToken', 'platform')

$getNotifications = $spec.paths.'/notifications'.get
$hasResponseSchema = $false
if ($null -ne $getNotifications.responses.'200'.content.'application/json'.schema) {
    $hasResponseSchema = $true
}

if (-not $hasResponseSchema) {
    Write-Warning "BACK-NOTIF-001 ABIERTO: GET /notifications no documenta schema de respuesta 200."
    Write-Warning "Android no puede tipar la bandeja sin contrato estable. Negociar con backend antes de cerrar Fase 7."
} else {
    Write-Host "GET /notifications tiene schema de respuesta documentado." -ForegroundColor Green
}

$soloNoLeidas = $getNotifications.parameters | Where-Object { $_.name -eq 'soloNoLeidas' }
if ($null -eq $soloNoLeidas) {
    Write-Warning "GET /notifications no documenta query param 'soloNoLeidas'."
} else {
    Write-Host "Query param soloNoLeidas documentado." -ForegroundColor Green
}

$markReadResponse = $spec.paths.'/notifications/{id}/read'.patch.responses.'200'.content.'application/json'.schema
if ($null -eq $markReadResponse) {
    Write-Warning "PATCH /notifications/{id}/read no documenta schema de respuesta."
} else {
    Write-Host "PATCH mark-as-read documentado." -ForegroundColor Green
}

Write-Host "`nVerificación Fase 7 (contrato notificaciones) finalizada." -ForegroundColor Cyan
if (-not $hasResponseSchema) { exit 2 }
