param(
    [Parameter(Mandatory = $true)][string]$Email,
    [Parameter(Mandatory = $true)][string]$Password,
    [long]$BarrioId = 94,
    [string]$BaseUrl = 'https://apioficios.pushsoftware.com.ar/'
)

$ErrorActionPreference = 'Stop'
$login = Invoke-RestMethod -Method Post -Uri ($BaseUrl + 'auth/login') -ContentType 'application/json' -Body (@{ email = $Email; contrasena = $Password } | ConvertTo-Json)
$headers = @{ Authorization = "Bearer $($login.access_token)" }
$addressId = $null
try {
    $createBody = @{
        barrioId = $BarrioId
        nombre = 'Domicilio E2E temporal'
        calle = 'Belgrano'
        numero = '1250'
        favorito = $false
    } | ConvertTo-Json
    $created = Invoke-RestMethod -Method Post -Uri ($BaseUrl + 'domicilios/mi') -Headers $headers -ContentType 'application/json' -Body $createBody
    $addressId = $created.id
    if ($null -eq $addressId) { throw 'POST /domicilios/mi no devolvió id.' }

    $updateBody = @{
        barrioId = $BarrioId
        nombre = 'Domicilio E2E actualizado'
        calle = 'Belgrano'
        numero = '1250'
        favorito = $false
    } | ConvertTo-Json
    Invoke-RestMethod -Method Patch -Uri ($BaseUrl + "domicilios/$addressId") -Headers $headers -ContentType 'application/json' -Body $updateBody | Out-Null
    $addresses = Invoke-RestMethod -Uri ($BaseUrl + 'domicilios/mis') -Headers $headers
    $saved = @($addresses.domicilios | Where-Object id -eq $addressId) | Select-Object -First 1
    if ($saved.nombre -ne 'Domicilio E2E actualizado') { throw 'El domicilio no persistió su actualización.' }

    $contacts = Invoke-RestMethod -Uri ($BaseUrl + 'contacto/mis') -Headers $headers
    $duplicateGroups = @($contacts.contactos | Group-Object telefonoWhatsapp | Where-Object Count -gt 1)
    Write-Output "FASE5_CLIENT_API_OK: domicilio_temporal=$addressId, contactos=$(@($contacts.contactos).Count), grupos_duplicados=$($duplicateGroups.Count)"
}
finally {
    if ($null -ne $addressId) {
        Invoke-RestMethod -Method Delete -Uri ($BaseUrl + "domicilios/$addressId") -Headers $headers | Out-Null
    }
}

$finalAddresses = Invoke-RestMethod -Uri ($BaseUrl + 'domicilios/mis') -Headers $headers
if (@($finalAddresses.domicilios | Where-Object id -eq $addressId).Count -gt 0) { throw 'El domicilio temporal no fue limpiado.' }
Write-Output 'FASE5_CLIENT_API_CLEANUP_OK: no quedaron domicilios temporales.'
