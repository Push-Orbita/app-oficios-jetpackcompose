package com.cocido.oficios.core.data.mapper

import com.cocido.oficios.core.network.api.GreetingDto
import com.cocido.oficios.domain.model.HomeContent

fun GreetingDto.toDomain(): HomeContent = HomeContent(
    title = title,
    description = body,
)
