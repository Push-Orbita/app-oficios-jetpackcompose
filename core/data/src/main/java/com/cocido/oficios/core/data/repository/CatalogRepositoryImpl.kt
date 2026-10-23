package com.cocido.oficios.core.data.repository

import com.cocido.oficios.core.common.result.AppResult
import com.cocido.oficios.core.common.result.map
import com.cocido.oficios.core.data.mapper.toCatalogOption
import com.cocido.oficios.core.data.remote.CatalogRemoteDataSource
import com.cocido.oficios.domain.model.CatalogOption
import com.cocido.oficios.domain.repository.CatalogRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CatalogRepositoryImpl @Inject constructor(
    private val catalogRemoteDataSource: CatalogRemoteDataSource,
) : CatalogRepository {

    @Volatile
    private var cachedRubros: List<CatalogOption>? = null

    @Volatile
    private var cachedSubRubros: List<CatalogOption>? = null

    override suspend fun getCategories(): AppResult<List<CatalogOption>> =
        catalogRemoteDataSource.fetchCategories().map { categories ->
            categories.map { it.toCatalogOption() }
        }

    override suspend fun getRubrosByCategory(categoryId: Long): AppResult<List<CatalogOption>> =
        catalogRemoteDataSource.fetchRubros(categoryId).map { rubros ->
            rubros.map { it.toCatalogOption() }
        }

    override suspend fun getSubRubrosByRubro(rubroId: Long): AppResult<List<CatalogOption>> =
        catalogRemoteDataSource.fetchSubRubros(rubroId).map { subRubros ->
            subRubros.map { it.toCatalogOption() }
        }

    override suspend fun getAllRubros(): AppResult<List<CatalogOption>> {
        cachedRubros?.let { return AppResult.Success(it) }
        return catalogRemoteDataSource.fetchAllRubros().map { rubros ->
            rubros.map { it.toCatalogOption() }.also { cachedRubros = it }
        }
    }

    override suspend fun getAllSubRubros(): AppResult<List<CatalogOption>> {
        cachedSubRubros?.let { return AppResult.Success(it) }
        return catalogRemoteDataSource.fetchAllSubRubros().map { subRubros ->
            subRubros.map { it.toCatalogOption() }.also { cachedSubRubros = it }
        }
    }

    override suspend fun searchBarrios(query: String): AppResult<List<CatalogOption>> {
        val trimmed = query.trim()
        val pageSize = if (trimmed.isEmpty()) 100 else 20
        return when (
            val first = catalogRemoteDataSource.fetchBarrios(
                query = trimmed.ifEmpty { null },
                nombre = trimmed.ifEmpty { null },
                pageSize = pageSize,
            )
        ) {
            is AppResult.Error -> first
            is AppResult.Success -> {
                val options = first.data.map { it.toCatalogOption() }
                if (trimmed.isEmpty()) {
                    AppResult.Success(options)
                } else {
                    val filtered = filterBarrios(options, trimmed)
                    if (filtered.isNotEmpty()) {
                        AppResult.Success(filtered)
                    } else {
                        catalogRemoteDataSource.fetchBarrios(
                            query = null,
                            nombre = null,
                            pageSize = 100,
                        ).map { barrios ->
                            filterBarrios(barrios.map { it.toCatalogOption() }, trimmed)
                        }
                    }
                }
            }
        }
    }
}

internal fun filterBarrios(options: List<CatalogOption>, query: String): List<CatalogOption> {
    val needle = query.foldForSearch()
    if (needle.isBlank()) return options
    return options.filter { option -> option.label.foldForSearch().contains(needle) }
}

internal fun String.foldForSearch(): String =
    java.text.Normalizer.normalize(lowercase(), java.text.Normalizer.Form.NFD)
        .replace("\\p{M}+".toRegex(), "")
