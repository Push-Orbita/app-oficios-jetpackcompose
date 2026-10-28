package com.cocido.oficios.core.data.repository

import com.cocido.oficios.core.common.result.AppResult
import com.cocido.oficios.core.data.cache.ClientProfileStore
import com.cocido.oficios.core.data.cache.SessionCacheCoordinator
import com.cocido.oficios.core.data.remote.AccountRemoteDataSource
import com.cocido.oficios.core.data.remote.AgendaRemoteDataSource
import com.cocido.oficios.core.data.remote.ClientProfileRemoteDataSource
import com.cocido.oficios.core.data.remote.ProviderProfileRemoteDataSource
import com.cocido.oficios.core.data.remote.WorkCertificateRemoteDataSource
import com.cocido.oficios.core.data.remote.WorkGalleryRemoteDataSource
import com.cocido.oficios.core.data.session.SessionManager
import com.cocido.oficios.core.datastore.TokenDataStore
import com.cocido.oficios.core.network.api.account.AccountApi
import com.cocido.oficios.core.network.api.account.AccountProfileUploadDataSource
import com.cocido.oficios.core.network.api.account.WorkGalleryApi
import com.cocido.oficios.core.network.api.account.WorkGalleryUploadDataSource
import com.cocido.oficios.core.network.api.account.dto.AccountDeletionRequestDto
import com.cocido.oficios.core.network.api.account.dto.ChangePasswordRequestDto
import com.cocido.oficios.core.network.api.account.dto.CreateAccountDeletionRequestDto
import com.cocido.oficios.core.network.api.account.dto.WorkGalleryDto
import com.cocido.oficios.core.network.api.auth.AuthApi
import com.cocido.oficios.core.network.api.auth.dto.ClientePerfilEstadoDto
import com.cocido.oficios.core.network.api.auth.dto.ClientePerfilMeDto
import com.cocido.oficios.core.network.api.auth.dto.LoginRequestDto
import com.cocido.oficios.core.network.api.auth.dto.LoginResponseDto
import com.cocido.oficios.core.network.api.auth.dto.MessageResponseDto
import com.cocido.oficios.core.network.api.auth.dto.PersonaDto
import com.cocido.oficios.core.network.api.auth.dto.RefreshTokenRequestDto
import com.cocido.oficios.core.network.api.auth.dto.RefreshTokenResponseDto
import com.cocido.oficios.core.network.api.auth.dto.RequestPasswordResetCodeDto
import com.cocido.oficios.core.network.api.auth.dto.ResetPasswordWithCodeDto
import com.cocido.oficios.core.network.api.auth.dto.RolUsuarioDto
import com.cocido.oficios.core.network.api.auth.dto.SignupResponseDto
import com.cocido.oficios.core.network.api.auth.dto.TrabajadorOficioPerfilMeDto
import com.cocido.oficios.core.network.api.auth.dto.TrabajadorPerfilEstadoDto
import com.cocido.oficios.core.network.api.auth.dto.UsuarioDto
import com.cocido.oficios.core.network.api.auth.dto.VerifyEmailRequestDto
import com.cocido.oficios.core.network.api.auth.dto.VerifyPasswordResetCodeDto
import com.cocido.oficios.core.network.api.profile.ClientProfileApi
import com.cocido.oficios.core.network.api.profile.ProviderProfileApi
import com.cocido.oficios.core.network.api.profile.WorkCertificateApi
import com.cocido.oficios.core.network.api.profile.WorkCertificateUploadDataSource
import com.cocido.oficios.core.network.api.profile.dto.CreateContactoRequestDto
import com.cocido.oficios.core.network.api.profile.dto.CreateContactoResponseDto
import com.cocido.oficios.core.network.api.profile.dto.CreateDomicilioRequestDto
import com.cocido.oficios.core.network.api.profile.dto.CreateFullAltaRubrosSubrubrosRequestDto
import com.cocido.oficios.core.network.api.profile.dto.EnviarCodigoTelefonoResponseDto
import com.cocido.oficios.core.network.api.profile.dto.MisContactosResponseDto
import com.cocido.oficios.core.network.api.profile.dto.MisDomiciliosResponseDto
import com.cocido.oficios.core.network.api.profile.dto.UpdateContactoRequestDto
import com.cocido.oficios.core.network.api.profile.dto.UpdateDomicilioRequestDto
import com.cocido.oficios.core.network.api.profile.dto.UpdateFavoriteRequestDto
import com.cocido.oficios.core.network.api.profile.dto.UpdateTrabajadorPerfilProfesionalRequestDto
import com.cocido.oficios.core.network.api.profile.dto.VerifyTelefonoCodigoRequestDto
import com.cocido.oficios.core.network.api.profile.dto.VerificarTelefonoCodigoResponseDto
import com.cocido.oficios.domain.model.ProviderRubroFullRequest
import com.cocido.oficios.domain.model.RegisterTradeTimeSlotDraft
import com.cocido.oficios.domain.model.UpdateAccountProfileRequest
import java.io.File
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.JsonElement
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import retrofit2.HttpException
import retrofit2.Response

@RunWith(RobolectricTestRunner::class)
class AccountRepositoryCacheTest {

    private lateinit var authApi: FakeAuthApi
    private lateinit var accountApi: FakeAccountApi
    private lateinit var galleryApi: FakeWorkGalleryApi
    private lateinit var repository: AccountRepositoryImpl
    private lateinit var coordinator: SessionCacheCoordinator

    @Before
    fun setUp() {
        authApi = FakeAuthApi()
        accountApi = FakeAccountApi()
        galleryApi = FakeWorkGalleryApi(authApi)
        coordinator = SessionCacheCoordinator()
        val context = RuntimeEnvironment.getApplication()
        val sessionManager = SessionManager(TokenDataStore(context), coordinator)
        repository = AccountRepositoryImpl(
            accountRemote = AccountRemoteDataSource(accountApi, AccountProfileUploadDataSource(accountApi)),
            clientRemote = ClientProfileRemoteDataSource(UnusedClientProfileApi()),
            providerRemote = ProviderProfileRemoteDataSource(UnusedProviderProfileApi(), authApi),
            agendaRemote = AgendaRemoteDataSource(UnusedAgendaApi()),
            certificateRemote = WorkCertificateRemoteDataSource(
                UnusedWorkCertificateApi(),
                WorkCertificateUploadDataSource(UnusedWorkCertificateApi()),
            ),
            galleryRemote = WorkGalleryRemoteDataSource(galleryApi, WorkGalleryUploadDataSource(galleryApi)),
            sessionManager = sessionManager,
            coordinator = coordinator,
            clientProfileStore = ClientProfileStore(coordinator),
        )
    }

    @Test
    fun `upload and delete gallery emit the new provider details`() = runTest {
        authApi.gallery = emptyList()
        assertTrue(repository.getGallery() is AppResult.Success)
        assertEquals(emptyList<Long>(), repository.providerDetails.value?.gallery?.map { it.id }.orEmpty())

        val photo = File.createTempFile("galeria", ".jpg").apply {
            writeBytes(byteArrayOf(0xFF.toByte(), 0xD8.toByte(), 0xFF.toByte(), 0xE0.toByte(), 0, 16, 0, 0))
        }
        val uploaded = repository.uploadGallery(listOf(photo.absolutePath))
        assertTrue(uploaded is AppResult.Success)
        assertEquals(listOf(99L), repository.providerDetails.value?.gallery?.map { it.id })

        val deleted = repository.deleteGalleryItem(99L)
        assertTrue(deleted is AppResult.Success)
        assertEquals(emptyList<Long>(), repository.providerDetails.value?.gallery?.map { it.id }.orEmpty())
    }

    @Test
    fun `update profile emits the new cached name`() = runTest {
        accountApi.currentUser = clientUser("Juan")
        accountApi.updatedUser = clientUser("Pedro")
        assertTrue(repository.getProfile() is AppResult.Success)
        assertEquals("Juan", repository.profile.value?.user?.firstName)

        val result = repository.updateProfile(
            UpdateAccountProfileRequest(
                userId = 1L,
                firstName = "Pedro",
                lastName = "Pérez",
                email = "user@test.com",
            ),
        )
        assertTrue(result is AppResult.Success)
        assertEquals("Pedro", repository.profile.value?.user?.firstName)
    }

    @Test
    fun `logout coordinator clears gallery cache`() = runTest {
        authApi.gallery = listOf(WorkGalleryDto(7L, "https://cdn/7.jpg"))
        assertTrue(repository.getGallery() is AppResult.Success)
        assertEquals(1, repository.providerDetails.value?.gallery?.size)

        coordinator.clearAll()
        assertEquals(null, repository.providerDetails.value)
        assertEquals(null, repository.profile.value)
    }

    @Test
    fun `replace does not delete when create returns 400`() = runTest {
        val api = RecordingProviderProfileApi(createSucceeds = false)
        repository = repositoryWith(providerApi = api)
        val result = repository.replaceProviderService(11L, true, sampleReplaceRequest())
        assertTrue(result is AppResult.Error)
        assertEquals(1, api.createFullCalls)
        assertEquals(emptyList<Long>(), api.deletedIds)
    }

    @Test
    fun `replace deletes old rubro only after create 2xx`() = runTest {
        val api = RecordingProviderProfileApi(createSucceeds = true)
        repository = repositoryWith(providerApi = api)
        val result = repository.replaceProviderService(11L, true, sampleReplaceRequest())
        assertTrue(result is AppResult.Success)
        assertEquals(1, api.createFullCalls)
        assertEquals(listOf(11L), api.deletedIds)
    }

    @Test
    fun `edit cached service uses PATCH and creates sesion when needed`() = runTest {
        val api = RecordingProviderProfileApi(createSucceeds = true)
        val agenda = RecordingAgendaApi()
        authApi.rubros = listOf(existingRubroDto())
        accountApi.currentUser = providerUser()
        repository = repositoryWith(providerApi = api, agendaApi = agenda)
        assertTrue(repository.getProfile() is AppResult.Success)
        val result = repository.replaceProviderService(11L, true, sampleReplaceRequest())
        assertTrue(result is AppResult.Success)
        assertEquals(1, api.updateRubroFullCalls)
        assertEquals(0, api.createFullCalls)
        assertEquals(emptyList<Long>(), api.deletedIds)
        assertEquals(1, agenda.createSesionCalls)
        assertEquals(emptyList<Long>(), agenda.deletedSesionIds)
    }

    @Test
    fun `disabling turns uses PATCH and does not delete sesion`() = runTest {
        val api = RecordingProviderProfileApi(createSucceeds = true)
        val agenda = RecordingAgendaApi()
        authApi.rubros = listOf(existingRubroDto(requiresTurns = true))
        agenda.sessions = listOf(
            com.cocido.oficios.core.network.api.agenda.dto.SesionDto(
                id = 7L,
                duracion = 60,
                medidaDuracion = "minutos",
                trabajadorOficioSubRubroId = 21L,
            ),
        )
        accountApi.currentUser = providerUser()
        repository = repositoryWith(providerApi = api, agendaApi = agenda)
        assertTrue(repository.getProfile() is AppResult.Success)
        val result = repository.replaceProviderService(
            11L,
            true,
            sampleReplaceRequest().copy(requiresTurns = false),
        )
        assertTrue(result is AppResult.Success)
        assertEquals(1, api.updateRubroFullCalls)
        assertEquals(0, api.createFullCalls)
        assertEquals(emptyList<Long>(), api.deletedIds)
        assertEquals(0, agenda.createSesionCalls)
        assertEquals(emptyList<Long>(), agenda.deletedSesionIds)
        assertEquals(true, repository.providerDetails.value?.services?.single()?.subServices?.single()?.requiresTurns)
    }

    @Test
    fun `editing specialty ids uses PATCH and does not recreate service`() = runTest {
        val api = RecordingProviderProfileApi(createSucceeds = true)
        authApi.rubros = listOf(existingRubroDto())
        accountApi.currentUser = providerUser()
        repository = repositoryWith(providerApi = api, agendaApi = RecordingAgendaApi())
        assertTrue(repository.getProfile() is AppResult.Success)
        val result = repository.replaceProviderService(
            11L,
            true,
            sampleReplaceRequest().copy(subRubroIds = listOf(100, 200), requiresTurns = false),
        )
        assertTrue(result is AppResult.Success)
        assertEquals(1, api.updateRubroFullCalls)
        assertEquals(0, api.createFullCalls)
        assertEquals(emptyList<Long>(), api.deletedIds)
        assertEquals(listOf(100L), repository.providerDetails.value?.services?.single()?.subServices?.map { it.catalogSubRubroId })
    }

    private fun repositoryWith(
        providerApi: ProviderProfileApi = UnusedProviderProfileApi(),
        agendaApi: com.cocido.oficios.core.network.api.agenda.AgendaApi = UnusedAgendaApi(),
    ): AccountRepositoryImpl {
        val context = RuntimeEnvironment.getApplication()
        val sessionManager = SessionManager(TokenDataStore(context), coordinator)
        return AccountRepositoryImpl(
            accountRemote = AccountRemoteDataSource(accountApi, AccountProfileUploadDataSource(accountApi)),
            clientRemote = ClientProfileRemoteDataSource(UnusedClientProfileApi()),
            providerRemote = ProviderProfileRemoteDataSource(providerApi, authApi),
            agendaRemote = AgendaRemoteDataSource(agendaApi),
            certificateRemote = WorkCertificateRemoteDataSource(
                UnusedWorkCertificateApi(),
                WorkCertificateUploadDataSource(UnusedWorkCertificateApi()),
            ),
            galleryRemote = WorkGalleryRemoteDataSource(galleryApi, WorkGalleryUploadDataSource(galleryApi)),
            sessionManager = sessionManager,
            coordinator = coordinator,
            clientProfileStore = ClientProfileStore(coordinator),
        )
    }
}

private fun clientUser(firstName: String) = UsuarioDto(
    id = 1L,
    email = "user@test.com",
    persona = PersonaDto(id = 2L, nombres = firstName, apellido = "Pérez"),
    rolUsuario = RolUsuarioDto(id = 1L, nombre = "cliente"),
)

private class FakeAuthApi : AuthApi {
    var gallery: List<WorkGalleryDto> = emptyList()
    var rubros: List<com.cocido.oficios.core.network.api.profile.dto.TrabajadorOficioRubroFullDto> = emptyList()

    override suspend fun getProviderProfile() = TrabajadorOficioPerfilMeDto(
        id = 9L,
        estado = "VALIDADO",
        rubros = rubros,
        galeriaTrabajos = gallery,
    )

    override suspend fun login(request: LoginRequestDto) = unused()
    override suspend fun signup(
        firstName: RequestBody,
        lastName: RequestBody,
        email: RequestBody,
        password: RequestBody,
        roleId: RequestBody,
        document: RequestBody?,
    ): SignupResponseDto = unused()
    override suspend fun refreshToken(request: RefreshTokenRequestDto): RefreshTokenResponseDto = unused()
    override suspend fun getCurrentUser(): UsuarioDto = unused()
    override suspend fun logout() = unused()
    override suspend fun requestPasswordReset(request: RequestPasswordResetCodeDto) = unused()
    override suspend fun verifyPasswordResetCode(request: VerifyPasswordResetCodeDto): Map<String, String> = unused()
    override suspend fun resetPassword(authorization: String, request: ResetPasswordWithCodeDto) = unused()
    override suspend fun sendVerificationEmail(): MessageResponseDto = unused()
    override suspend fun verifyEmail(request: VerifyEmailRequestDto) = unused()
    override suspend fun searchUserRoles(pageSize: Int, pageNumber: Int): Map<String, JsonElement> = unused()
    override suspend fun getClientProfileStatus(): ClientePerfilEstadoDto = unused()
    override suspend fun getClientProfile(): ClientePerfilMeDto = unused()
    override suspend fun getProviderProfileStatus(): TrabajadorPerfilEstadoDto = unused()
}

private class FakeAccountApi : AccountApi {
    var currentUser: UsuarioDto = clientUser("Juan")
    var updatedUser: UsuarioDto = currentUser

    override suspend fun getCurrentUser() = currentUser
    override suspend fun updateUser(
        userId: Long,
        firstName: RequestBody,
        lastName: RequestBody,
        email: RequestBody,
        profilePhoto: MultipartBody.Part?,
        currentPhotoUrl: RequestBody?,
    ) = updatedUser
    override suspend fun changePassword(request: ChangePasswordRequestDto) = unused()
    override suspend fun createDeletionRequest(
        idempotencyKey: String,
        request: CreateAccountDeletionRequestDto,
    ): AccountDeletionRequestDto = unused()
    override suspend fun getCurrentDeletionRequest(): AccountDeletionRequestDto = unused()
    override suspend fun cancelCurrentDeletionRequest(): AccountDeletionRequestDto = unused()
}

private class FakeWorkGalleryApi(
    private val authApi: FakeAuthApi,
) : WorkGalleryApi {
    override suspend fun search(workerId: Long, pageSize: Int, pageNumber: Int): JsonElement = unused()
    override suspend fun upload(photos: List<MultipartBody.Part>): List<WorkGalleryDto> {
        val item = WorkGalleryDto(id = 99L, urlArchivo = "https://cdn/99.jpg")
        authApi.gallery = authApi.gallery + item
        return listOf(item)
    }
    override suspend fun delete(id: Long) {
        authApi.gallery = authApi.gallery.filterNot { it.id == id }
    }
}

private class UnusedClientProfileApi : ClientProfileApi {
    override suspend fun getMyDomicilios(): MisDomiciliosResponseDto = unused()
    override suspend fun getMyContactos(): MisContactosResponseDto = unused()
    override suspend fun createDomicilio(request: CreateDomicilioRequestDto) = unused()
    override suspend fun createMyDomicilio(request: UpdateDomicilioRequestDto) = unused()
    override suspend fun createContacto(request: CreateContactoRequestDto): CreateContactoResponseDto = unused()
    override suspend fun sendPhoneVerificationCode(contactId: Long): EnviarCodigoTelefonoResponseDto = unused()
    override suspend fun verifyPhoneCode(
        contactId: Long,
        request: VerifyTelefonoCodigoRequestDto,
    ): VerificarTelefonoCodigoResponseDto = unused()
    override suspend fun updateDomicilio(id: Long, request: UpdateDomicilioRequestDto) = unused()
    override suspend fun deleteDomicilio(id: Long) = unused()
    override suspend fun setFavoriteDomicilio(id: Long, request: UpdateFavoriteRequestDto) = unused()
    override suspend fun updateContacto(id: Long, request: UpdateContactoRequestDto) = unused()
    override suspend fun deleteContacto(id: Long) = unused()
    override suspend fun setFavoriteContacto(id: Long, request: UpdateFavoriteRequestDto) = unused()
}

private class UnusedAgendaApi : com.cocido.oficios.core.network.api.agenda.AgendaApi {
    override suspend fun getSlotsDisponibles(
        subRubroId: Long,
        fechaDesde: String,
        fechaHasta: String,
        fecha: String?,
    ) = unused()
    override suspend fun searchSlots(
        subRubroId: Long,
        fromIso: String?,
        toIso: String?,
        pageSize: Int,
    ) = unused()
    override suspend fun createSlot(request: com.cocido.oficios.core.network.api.agenda.dto.CreateSlotTurnoRequestDto) =
        unused()
    override suspend fun updateSlot(
        id: Long,
        request: com.cocido.oficios.core.network.api.agenda.dto.UpdateSlotTurnoRequestDto,
    ) = unused()
    override suspend fun deleteSlot(id: Long) = unused()
    override suspend fun getSesionesPorSubRubro(subRubroId: Long) = unused()
    override suspend fun createSesion(request: com.cocido.oficios.core.network.api.agenda.dto.CreateSesionRequestDto) =
        unused()
    override suspend fun deleteSesion(id: Long) = unused()
}

private fun providerUser() = UsuarioDto(
    id = 9L,
    email = "t@test.com",
    persona = PersonaDto(id = 2L, nombres = "Emanuel", apellido = "Molina"),
    rolUsuario = RolUsuarioDto(id = 2L, nombre = "trabajador"),
)

private fun existingRubroDto(
    requiresTurns: Boolean = false,
) = com.cocido.oficios.core.network.api.profile.dto.TrabajadorOficioRubroFullDto(
    id = 11L,
    rubro = com.cocido.oficios.core.network.api.catalog.dto.RubroDto(id = 10L, nombre = "Plomería"),
    principal = true,
    subRubros = listOf(
        com.cocido.oficios.core.network.api.profile.dto.TrabajadorOficioSubRubroFullDto(
            id = 21L,
            subRubro = com.cocido.oficios.core.network.api.catalog.dto.SubRubroDto(id = 100L, nombre = "Caños"),
            lugarDomicilio = true,
            requiereTurnos = requiresTurns,
        ),
    ),
)

private class RecordingAgendaApi : com.cocido.oficios.core.network.api.agenda.AgendaApi {
    var createSesionCalls = 0
    val deletedSesionIds = mutableListOf<Long>()
    var sessions = emptyList<com.cocido.oficios.core.network.api.agenda.dto.SesionDto>()
    override suspend fun getSlotsDisponibles(
        subRubroId: Long,
        fechaDesde: String,
        fechaHasta: String,
        fecha: String?,
    ) = unused()
    override suspend fun searchSlots(
        subRubroId: Long,
        fromIso: String?,
        toIso: String?,
        pageSize: Int,
    ) = unused()
    override suspend fun createSlot(request: com.cocido.oficios.core.network.api.agenda.dto.CreateSlotTurnoRequestDto) =
        unused()
    override suspend fun updateSlot(
        id: Long,
        request: com.cocido.oficios.core.network.api.agenda.dto.UpdateSlotTurnoRequestDto,
    ) = unused()
    override suspend fun deleteSlot(id: Long) = unused()
    override suspend fun getSesionesPorSubRubro(subRubroId: Long) = sessions
    override suspend fun createSesion(request: com.cocido.oficios.core.network.api.agenda.dto.CreateSesionRequestDto) =
        com.cocido.oficios.core.network.api.agenda.dto.SesionDto(
            id = 1L,
            duracion = request.duracion,
            medidaDuracion = request.medidaDuracion,
            intervalo = request.intervalo,
            medidaIntervalo = request.medidaIntervalo,
            trabajadorOficioSubRubroId = request.trabajadorOficioSubRubroId,
        ).also { createSesionCalls += 1 }
    override suspend fun deleteSesion(id: Long) {
        deletedSesionIds += id
        sessions = sessions.filterNot { it.id == id }
    }
}

private fun sampleReplaceRequest() = ProviderRubroFullRequest(
    rubroId = 10,
    subRubroIds = listOf(100),
    servesAtClientHome = true,
    servesAtLocal = false,
    workDays = listOf("LUNES"),
    timeSlots = listOf(RegisterTradeTimeSlotDraft("09:00", "18:00")),
    requiresTurns = true,
)

private class RecordingProviderProfileApi(
    private val createSucceeds: Boolean,
) : ProviderProfileApi {
    var createFullCalls = 0
    var updateRubroFullCalls = 0
    val deletedIds = mutableListOf<Long>()

    override suspend fun updateProfile(request: UpdateTrabajadorPerfilProfesionalRequestDto) = unused()
    override suspend fun createRubroFull(request: CreateFullAltaRubrosSubrubrosRequestDto) {
        createFullCalls += 1
        if (!createSucceeds) throw va01()
    }
    override suspend fun createSecondaryRubroFull(request: CreateFullAltaRubrosSubrubrosRequestDto) = unused()
    override suspend fun updateRubroFull(id: Long, request: CreateFullAltaRubrosSubrubrosRequestDto) {
        updateRubroFullCalls += 1
    }
    override suspend fun finalizeProfile(): TrabajadorPerfilEstadoDto = unused()
    override suspend fun deleteRubro(id: Long) {
        deletedIds += id
    }
}

private fun va01(): HttpException {
    val body = """{"code":"VA01","message":"El trabajador ya tiene un rubro principal configurado."}"""
        .toResponseBody("application/json".toMediaType())
    return HttpException(Response.error<Unit>(400, body))
}

private class UnusedProviderProfileApi : ProviderProfileApi {
    override suspend fun updateProfile(request: UpdateTrabajadorPerfilProfesionalRequestDto) = unused()
    override suspend fun createRubroFull(request: CreateFullAltaRubrosSubrubrosRequestDto) = unused()
    override suspend fun createSecondaryRubroFull(request: CreateFullAltaRubrosSubrubrosRequestDto) = unused()
    override suspend fun updateRubroFull(id: Long, request: CreateFullAltaRubrosSubrubrosRequestDto) = unused()
    override suspend fun finalizeProfile(): TrabajadorPerfilEstadoDto = unused()
    override suspend fun deleteRubro(id: Long) = unused()
}

private class UnusedWorkCertificateApi : WorkCertificateApi {
    override suspend fun uploadCertificate(
        nombre: RequestBody,
        certificadoArchivo: MultipartBody.Part,
    ) = unused()
    override suspend fun searchCertificates(pageSize: Int, pageNumber: Int): JsonElement = unused()
    override suspend fun updateCertificate(
        id: Long,
        nombre: RequestBody?,
        certificadoArchivoNuevo: MultipartBody.Part?,
        urlArchivoActual: RequestBody?,
    ) = unused()
    override suspend fun deleteCertificate(id: Long) = unused()
}

private fun unused(): Nothing = error("unused")
