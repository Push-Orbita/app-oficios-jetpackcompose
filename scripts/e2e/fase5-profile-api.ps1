param(
    [Parameter(Mandatory = $true)][string]$Email,
    [Parameter(Mandatory = $true)][string]$Password,
    [string]$BaseUrl = 'https://apioficios.pushsoftware.com.ar/'
)

$ErrorActionPreference = 'Stop'
$fixture = Resolve-Path (Join-Path $PSScriptRoot '..\..\logo_oficios.png')
$loginBody = @{ email = $Email; contrasena = $Password } | ConvertTo-Json
$login = Invoke-RestMethod -Method Post -Uri ($BaseUrl + 'auth/login') -ContentType 'application/json' -Body $loginBody
$token = $login.access_token
if ([string]::IsNullOrWhiteSpace($token)) { throw 'Login sin access_token.' }
$headers = @{ Authorization = "Bearer $token" }

$profile = Invoke-RestMethod -Method Get -Uri ($BaseUrl + 'trabajador-oficio/me') -Headers $headers
$professionalBody = @{
    descripcion = $profile.descripcion
    experiencia = $profile.experiencia
    documento = $profile.documento
    metodosPago = @($profile.metodosPago)
} | ConvertTo-Json -Depth 5
$updated = Invoke-RestMethod -Method Patch -Uri ($BaseUrl + 'trabajador-oficio/me') -Headers $headers -ContentType 'application/json' -Body $professionalBody
if ($updated.id -ne $profile.id) { throw 'PATCH profesional devolvió otro trabajador.' }

$client = [System.Net.Http.HttpClient]::new()
$client.DefaultRequestHeaders.Authorization = [System.Net.Http.Headers.AuthenticationHeaderValue]::new('Bearer', $token)
$galleryId = $null
$certificateId = $null
try {
    $galleryContent = [System.Net.Http.MultipartFormDataContent]::new()
    $galleryBytes = [System.IO.File]::ReadAllBytes($fixture)
    $galleryFile = [System.Net.Http.ByteArrayContent]::new($galleryBytes)
    $galleryFile.Headers.ContentType = [System.Net.Http.Headers.MediaTypeHeaderValue]::new('image/png')
    $galleryContent.Add($galleryFile, 'fotos', 'fase5-e2e.png')
    $galleryResponse = $client.PostAsync(($BaseUrl + 'galeria-trabajo/subir-fotos'), $galleryContent).Result
    $galleryRaw = $galleryResponse.Content.ReadAsStringAsync().Result
    if (-not $galleryResponse.IsSuccessStatusCode) { throw "Falló galería: $galleryRaw" }
    $galleryItems = @($galleryRaw | ConvertFrom-Json)
    $galleryId = $galleryItems[0].id
    if ($null -eq $galleryId) { throw 'Galería creada sin id.' }

    $certificateContent = [System.Net.Http.MultipartFormDataContent]::new()
    $certificateContent.Add([System.Net.Http.StringContent]::new('Certificado E2E temporal'), 'nombre')
    $certificateFile = [System.Net.Http.ByteArrayContent]::new($galleryBytes)
    $certificateFile.Headers.ContentType = [System.Net.Http.Headers.MediaTypeHeaderValue]::new('image/png')
    $certificateContent.Add($certificateFile, 'certificadoArchivo', 'fase5-cert-e2e.png')
    $certificateResponse = $client.PostAsync(($BaseUrl + 'certificado-trabajo'), $certificateContent).Result
    $certificateRaw = $certificateResponse.Content.ReadAsStringAsync().Result
    if (-not $certificateResponse.IsSuccessStatusCode) { throw "Falló certificado: $certificateRaw" }
    $certificateId = ($certificateRaw | ConvertFrom-Json).id
    if ($null -eq $certificateId) { throw 'Certificado creado sin id.' }

    Write-Output "FASE5_API_OK: perfil=$($profile.id), galeria_temporal=$galleryId, certificado_temporal=$certificateId"
}
finally {
    if ($null -ne $galleryId) {
        $deleteGallery = $client.DeleteAsync(($BaseUrl + "galeria-trabajo/$galleryId")).Result
        if (-not $deleteGallery.IsSuccessStatusCode) { Write-Warning "No se pudo limpiar galería temporal $galleryId" }
    }
    if ($null -ne $certificateId) {
        $deleteCertificate = $client.DeleteAsync(($BaseUrl + "certificado-trabajo/$certificateId")).Result
        if (-not $deleteCertificate.IsSuccessStatusCode) { Write-Warning "No se pudo limpiar certificado temporal $certificateId" }
    }
    $client.Dispose()
}

$finalProfile = Invoke-RestMethod -Method Get -Uri ($BaseUrl + 'trabajador-oficio/me') -Headers $headers
if (@($finalProfile.galeriaTrabajos | Where-Object id -eq $galleryId).Count -gt 0) { throw 'La foto temporal no fue limpiada.' }
if (@($finalProfile.certificados | Where-Object id -eq $certificateId).Count -gt 0) { throw 'El certificado temporal no fue limpiado.' }
Write-Output 'FASE5_API_CLEANUP_OK: no quedaron archivos temporales.'
