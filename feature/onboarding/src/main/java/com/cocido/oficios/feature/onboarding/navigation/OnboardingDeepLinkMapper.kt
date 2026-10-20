package com.cocido.oficios.feature.onboarding.navigation

import com.cocido.oficios.core.common.navigation.PushDeepLinkMapper

object OnboardingDeepLinkMapper {

    fun toInternalRoute(deepLink: String?): String? = PushDeepLinkMapper.toInternalRoute(deepLink)
}
