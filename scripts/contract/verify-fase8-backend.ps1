param([string]$OpenApiUrl = 'https://apioficios.pushsoftware.com.ar/api-json')

$ErrorActionPreference = 'Stop'
$spec = Invoke-RestMethod -Uri $OpenApiUrl

function Assert-OperationExists {
    param([string]$Method, [string]$Path)
    if ($null -eq $spec.paths.$Path -or $null -eq $spec.paths.$Path.$Method) {
        throw "Falta operación $($Method.ToUpper()) $Path"
    }
}

Assert-OperationExists 'get' '/agenda/slot/disponibles'
Assert-OperationExists 'post' '/solicitud-servicio'
Assert-OperationExists 'patch' '/booking/{id}/finalizar'

$finalize = $spec.paths.'/booking/{id}/finalizar'.patch
$finalizeBody = $finalize.requestBody.content.'application/json'.schema
if ($null -eq $finalizeBody) {
    Write-Warning "BACK-ORDERS-001 ABIERTO: PATCH /booking/{id}/finalizar no documenta body con código."
} else {
    Write-Host "PATCH finalizar documenta request body." -ForegroundColor Green
}

$simpleDto = $spec.components.schemas.SolicitudServicioSimpleDto
if ($null -ne $simpleDto -and $simpleDto.properties.trabajadorOficio) {
    Write-Host "BACK-ORDERS-003: SolicitudServicioSimpleDto incluye trabajadorOficio." -ForegroundColor Green
} else {
    Write-Warning "BACK-ORDERS-003 ABIERTO: listado cliente sin datos de trabajador en SolicitudServicioSimpleDto."
}

$slotConflict = $spec.components.responses.'409'.description
Write-Host "`nVerificación Fase 8 (contrato pedidos/agenda) finalizada." -ForegroundColor Cyan
