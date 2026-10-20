param([string]$OpenApiUrl = 'https://apioficios.pushsoftware.com.ar/api-json')

$ErrorActionPreference = 'Stop'
$spec = Invoke-RestMethod -Uri $OpenApiUrl

$requiredOperations = @(
    @('get', '/domicilios/mis'),
    @('post', '/domicilios/mi'),
    @('patch', '/domicilios/{id}'),
    @('post', '/tracking-booking/booking/{bookingId}/marcar'),
    @('get', '/tracking-booking/booking/{bookingId}')
)

$missing = foreach ($operation in $requiredOperations) {
    $method = $operation[0]
    $path = $operation[1]
    if ($null -eq $spec.paths.$path -or $null -eq $spec.paths.$path.$method) {
        "$($method.ToUpper()) $path"
    }
}

if ($missing.Count -gt 0) {
    throw "Faltan contratos requeridos por Fase 6: $($missing -join ', ')"
}

function Assert-Properties {
    param(
        [string]$SchemaName,
        [string[]]$RequiredProperties
    )
    $schema = $spec.components.schemas.$SchemaName
    if ($null -eq $schema) { throw "Swagger no publica el schema $SchemaName." }
    $missingProperties = $RequiredProperties | Where-Object {
        $_ -notin $schema.properties.PSObject.Properties.Name
    }
    if ($missingProperties.Count -gt 0) {
        throw "$SchemaName no publica: $($missingProperties -join ', ')"
    }
}

Assert-Properties 'CreateDomicilioFromTokenRequestDto' @('latitud', 'longitud')
Assert-Properties 'UpdateDomicilioRequestDto' @('latitud', 'longitud')
Assert-Properties 'DomicilioSinPersonaDTO' @('latitud', 'longitud')
Assert-Properties 'MarcarTrackingRequestDto' @('estado', 'lat', 'lng')
Assert-Properties 'TrackingBookingDTO' @('bookingId', 'trabajadorOficioId', 'estado', 'lat', 'lng', 'createdAt')

$trackingEnum = $spec.components.schemas.MarcarTrackingRequestDto.properties.estado.enum
foreach ($expectedState in @('EN_CAMINO', 'LLEGUE')) {
    if ($expectedState -notin $trackingEnum) {
        throw "MarcarTrackingRequestDto no documenta el estado $expectedState."
    }
}

$addressLat = $spec.components.schemas.CreateDomicilioFromTokenRequestDto.properties.latitud
$addressLng = $spec.components.schemas.CreateDomicilioFromTokenRequestDto.properties.longitud
if ($addressLat.type -ne 'number' -or $addressLng.type -ne 'number') {
    throw 'Las coordenadas de domicilio deben publicarse como números WGS84.'
}

$trackingPost = $spec.paths.'/tracking-booking/booking/{bookingId}/marcar'.post
$trackingGet = $spec.paths.'/tracking-booking/booking/{bookingId}'.get
if ($null -eq $trackingPost.security -or $null -eq $trackingGet.security) {
    throw 'Los endpoints de tracking deben documentar autenticación Bearer.'
}

Write-Output "FASE6_CONTRACT_OK: $($requiredOperations.Count) operaciones y 5 schemas verificados contra OpenAPI."
Write-Output 'COORDINATES_OK: domicilios usan latitud/longitud numéricas WGS84.'
Write-Output "TRACKING_STATES_OK: $($trackingEnum -join ', ')"
Write-Output 'EXPECTED_BACKEND_GAP: Swagger no define frecuencia, antigüedad máxima ni semántica de expiración del tracking.'
