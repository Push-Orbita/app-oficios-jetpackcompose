param([string]$OpenApiUrl = 'https://apioficios.pushsoftware.com.ar/api-json')

$ErrorActionPreference = 'Stop'
$spec = Invoke-RestMethod -Uri $OpenApiUrl

$requiredOperations = @(
    @('get', '/auth/me'),
    @('patch', '/auth/change-password'),
    @('patch', '/usuario/{id}'),
    @('get', '/domicilios/mis'),
    @('post', '/domicilios/mi'),
    @('patch', '/domicilios/{id}'),
    @('delete', '/domicilios/{id}'),
    @('get', '/contacto/mis'),
    @('post', '/contacto'),
    @('patch', '/contacto/{id}'),
    @('delete', '/contacto/{id}'),
    @('post', '/trabajador-oficio-rubro/full/secundario'),
    @('delete', '/trabajador-oficio-rubro/{id}'),
    @('post', '/certificado-trabajo'),
    @('patch', '/certificado-trabajo/{id}'),
    @('delete', '/certificado-trabajo/{id}'),
    @('post', '/galeria-trabajo/subir-fotos'),
    @('delete', '/galeria-trabajo/{id}')
)

$missing = foreach ($operation in $requiredOperations) {
    $method = $operation[0]
    $path = $operation[1]
    if ($null -eq $spec.paths.$path -or $null -eq $spec.paths.$path.$method) { "$($method.ToUpper()) $path" }
}

if ($missing.Count -gt 0) {
    throw "Faltan contratos requeridos por Fase 5: $($missing -join ', ')"
}

$selfAddressDescription = $spec.paths.'/domicilios/mi'.post.description
if ($selfAddressDescription -notmatch 'token') {
    throw 'POST /domicilios/mi no documenta que la identidad proviene del token.'
}

$unsafeDeleteExists = $null -ne $spec.paths.'/usuario/{id}'.delete
$safeSelfDeleteExists = $null -ne $spec.paths.'/usuario/me'.delete -or $null -ne $spec.paths.'/auth/me'.delete

Write-Output "FASE5_CONTRACT_OK: $($requiredOperations.Count) operaciones verificadas contra OpenAPI."
Write-Output "ACCOUNT_DELETE_STATUS: generic_id=$unsafeDeleteExists safe_self=$safeSelfDeleteExists"
if (-not $safeSelfDeleteExists) {
    Write-Output 'EXPECTED_BACKEND_GAP: no existe eliminación segura de la cuenta autenticada; Android la mantiene deshabilitada.'
}
