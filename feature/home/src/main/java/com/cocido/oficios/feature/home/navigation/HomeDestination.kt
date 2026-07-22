package com.cocido.oficios.feature.home.navigation

import android.net.Uri

object HomeDestination {
    const val graph = "home"
    const val inicio = "home/inicio"
    const val categories = "home/categories"
    const val rubros = "home/rubros/{categoryId}?categoryName={categoryName}"
    const val searchResults =
        "home/search/results?rubroId={rubroId}&subRubroId={subRubroId}&barrioId={barrioId}" +
            "&query={query}&rubroLabel={rubroLabel}&subRubroLabel={subRubroLabel}" +
            "&barrioLabel={barrioLabel}&categoryId={categoryId}&categoryLabel={categoryLabel}"
    const val workerProfile = "home/worker/{workerId}"
    const val hireStub = "home/hire/{workerId}"

    fun rubros(categoryId: Long, categoryName: String): String =
        "home/rubros/$categoryId?categoryName=${Uri.encode(categoryName)}"

    fun searchResults(
        rubroId: Long? = null,
        subRubroId: Long? = null,
        barrioId: Long? = null,
        query: String? = null,
        rubroLabel: String? = null,
        subRubroLabel: String? = null,
        barrioLabel: String? = null,
        categoryId: Long? = null,
        categoryLabel: String? = null,
    ): String = buildString {
        append("home/search/results")
        append("?rubroId=").append(rubroId ?: -1L)
        append("&subRubroId=").append(subRubroId ?: -1L)
        append("&barrioId=").append(barrioId ?: -1L)
        append("&query=").append(Uri.encode(query.orEmpty()))
        append("&rubroLabel=").append(Uri.encode(rubroLabel.orEmpty()))
        append("&subRubroLabel=").append(Uri.encode(subRubroLabel.orEmpty()))
        append("&barrioLabel=").append(Uri.encode(barrioLabel.orEmpty()))
        append("&categoryId=").append(categoryId ?: -1L)
        append("&categoryLabel=").append(Uri.encode(categoryLabel.orEmpty()))
    }

    fun workerProfile(workerId: Long): String = "home/worker/$workerId"

    fun hireStub(workerId: Long): String = "home/hire/$workerId"
}
