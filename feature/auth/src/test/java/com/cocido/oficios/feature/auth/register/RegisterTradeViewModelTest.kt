package com.cocido.oficios.feature.auth.register

import com.cocido.oficios.core.common.result.AppError
import com.cocido.oficios.core.common.result.AppResult
import com.cocido.oficios.domain.model.CatalogOption
import com.cocido.oficios.domain.model.ProviderPaymentMethods
import com.cocido.oficios.domain.model.RegisterTradeDraft
import com.cocido.oficios.domain.repository.CatalogRepository
import com.cocido.oficios.domain.repository.RegisterTradeDraftRepository
import com.cocido.oficios.domain.usecase.catalog.GetCategoriesUseCase
import com.cocido.oficios.domain.usecase.catalog.GetRubrosByCategoryUseCase
import com.cocido.oficios.domain.usecase.catalog.GetSubRubrosByRubroUseCase
import com.cocido.oficios.domain.usecase.catalog.SearchBarriosUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RegisterTradeViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val fakeCatalogRepository = FakeCatalogRepository()
    private val fakeDraftRepository = FakeRegisterTradeDraftRepository()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loads categories from backend on init`() = runTest {
        fakeCatalogRepository.categories = listOf(
            CatalogOption(id = 1, label = "Hogar y Construcción"),
        )

        val viewModel = createViewModel()
        advanceUntilIdle()

        assertEquals(listOf("Hogar y Construcción"), viewModel.uiState.value.categories)
    }

    @Test
    fun `category change loads rubros from backend`() = runTest {
        fakeCatalogRepository.categories = listOf(CatalogOption(id = 1, label = "Hogar y Construcción"))
        fakeCatalogRepository.rubros = listOf(CatalogOption(id = 10, label = "Plomería"))

        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onEvent(RegisterTradeEvent.CategoryChanged("Hogar y Construcción"))
        advanceUntilIdle()

        assertEquals(listOf("Plomería"), viewModel.uiState.value.trades)
    }

    @Test
    fun `continue saves draft with selected ids`() = runTest {
        fakeCatalogRepository.categories = listOf(CatalogOption(id = 1, label = "Hogar y Construcción"))
        fakeCatalogRepository.rubros = listOf(CatalogOption(id = 10, label = "Plomería"))
        fakeCatalogRepository.subRubros = listOf(CatalogOption(id = 100, label = "Desatasco"))

        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onEvent(RegisterTradeEvent.CategoryChanged("Hogar y Construcción"))
        advanceUntilIdle()
        viewModel.onEvent(RegisterTradeEvent.TradeChanged("Plomería"))
        advanceUntilIdle()
        viewModel.onEvent(RegisterTradeEvent.SpecializationToggled("100", checked = true))
        viewModel.onEvent(RegisterTradeEvent.ExperienceRangeChanged("0 a 2 años"))
        viewModel.onEvent(RegisterTradeEvent.NeighborhoodQueryChanged("Centro"))
        viewModel.onEvent(RegisterTradeEvent.NeighborhoodSelected(CatalogOption(id = 5, label = "Centro")))
        viewModel.onEvent(RegisterTradeEvent.PaymentMethodToggled(ProviderPaymentMethods.EFECTIVO, checked = true))
        viewModel.onEvent(RegisterTradeEvent.TimeSlotFromChanged(0, "08:00"))
        viewModel.onEvent(RegisterTradeEvent.TimeSlotToChanged(0, "12:00"))
        viewModel.onEvent(RegisterTradeEvent.Continue)
        advanceUntilIdle()

        val draft = fakeDraftRepository.savedDraft
        assertNotNull(draft)
        assertEquals(1L, draft?.categoryId)
        assertEquals(10L, draft?.rubroId)
        assertEquals(listOf(100L), draft?.subRubroIds)
        assertEquals(5L, draft?.barrioId)
    }

    @Test
    fun `continue saves work days and service location flags`() = runTest {
        fakeCatalogRepository.categories = listOf(CatalogOption(id = 1, label = "Hogar y Construcción"))
        fakeCatalogRepository.rubros = listOf(CatalogOption(id = 10, label = "Plomería"))
        fakeCatalogRepository.subRubros = listOf(CatalogOption(id = 100, label = "Desatasco"))

        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onEvent(RegisterTradeEvent.CategoryChanged("Hogar y Construcción"))
        advanceUntilIdle()
        viewModel.onEvent(RegisterTradeEvent.TradeChanged("Plomería"))
        advanceUntilIdle()
        viewModel.onEvent(RegisterTradeEvent.SpecializationToggled("100", checked = true))
        viewModel.onEvent(RegisterTradeEvent.ExperienceRangeChanged("0 a 2 años"))
        viewModel.onEvent(RegisterTradeEvent.NeighborhoodQueryChanged("Centro"))
        viewModel.onEvent(RegisterTradeEvent.NeighborhoodSelected(CatalogOption(id = 5, label = "Centro")))
        viewModel.onEvent(RegisterTradeEvent.PaymentMethodToggled(ProviderPaymentMethods.EFECTIVO, checked = true))
        viewModel.onEvent(RegisterTradeEvent.WorkDayToggled("SABADO", checked = true))
        viewModel.onEvent(RegisterTradeEvent.ServesAtLocalChanged(true))
        viewModel.onEvent(RegisterTradeEvent.TimeSlotFromChanged(0, "08:00"))
        viewModel.onEvent(RegisterTradeEvent.TimeSlotToChanged(0, "12:00"))
        viewModel.onEvent(RegisterTradeEvent.Continue)
        advanceUntilIdle()

        val draft = fakeDraftRepository.savedDraft
        assertNotNull(draft)
        assertTrue(draft!!.workDays.contains("SABADO"))
        assertTrue(draft.servesAtLocal)
    }

    @Test
    fun `catalog error is exposed when categories fail`() = runTest {
        fakeCatalogRepository.categoriesResult = AppResult.Error(AppError.Network("Sin conexión"))

        val viewModel = createViewModel()
        advanceUntilIdle()

        assertEquals("Sin conexión", viewModel.uiState.value.catalogError)
        assertTrue(viewModel.uiState.value.categoryOptions.isEmpty())
    }

    private fun createViewModel(): RegisterTradeViewModel = RegisterTradeViewModel(
        getCategoriesUseCase = GetCategoriesUseCase(fakeCatalogRepository),
        getRubrosByCategoryUseCase = GetRubrosByCategoryUseCase(fakeCatalogRepository),
        getSubRubrosByRubroUseCase = GetSubRubrosByRubroUseCase(fakeCatalogRepository),
        searchBarriosUseCase = SearchBarriosUseCase(fakeCatalogRepository),
        registerTradeDraftRepository = fakeDraftRepository,
    )

    private class FakeCatalogRepository : CatalogRepository {
        var categories = emptyList<CatalogOption>()
        var rubros = emptyList<CatalogOption>()
        var subRubros = emptyList<CatalogOption>()
        var categoriesResult: AppResult<List<CatalogOption>>? = null

        override suspend fun getCategories(): AppResult<List<CatalogOption>> =
            categoriesResult ?: AppResult.Success(categories)

        override suspend fun getRubrosByCategory(categoryId: Long): AppResult<List<CatalogOption>> =
            AppResult.Success(rubros)

        override suspend fun getSubRubrosByRubro(rubroId: Long): AppResult<List<CatalogOption>> =
            AppResult.Success(subRubros)

        override suspend fun searchBarrios(query: String): AppResult<List<CatalogOption>> =
            AppResult.Success(listOf(CatalogOption(id = 5, label = "Centro")))
    }

    private class FakeRegisterTradeDraftRepository : RegisterTradeDraftRepository {
        var savedDraft: RegisterTradeDraft? = null

        override suspend fun save(draft: RegisterTradeDraft) {
            savedDraft = draft
        }

        override suspend fun get(): RegisterTradeDraft? = savedDraft

        override suspend fun clear() {
            savedDraft = null
        }
    }
}
