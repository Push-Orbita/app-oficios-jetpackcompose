package com.cocido.oficios.core.network.api.notifications

import com.cocido.oficios.core.network.api.notifications.dto.MarkNotificationReadResponseDto
import com.cocido.oficios.core.network.api.notifications.dto.NotificationsPageDto
import com.cocido.oficios.core.network.api.notifications.dto.RegisterDeviceTokenRequestDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface NotificationsApi {

    /** OpenAPI 201 sin content. */
    @POST("notifications/device-token")
    suspend fun registerDeviceToken(
        @Body request: RegisterDeviceTokenRequestDto,
    )

    @PATCH("notifications/{id}/read")
    suspend fun markAsRead(
        @Path("id") notificationId: Long,
    ): MarkNotificationReadResponseDto

    @GET("notifications")
    suspend fun getNotifications(
        @Query("pageSize") pageSize: Int = 20,
        @Query("pageNumber") pageNumber: Int = 1,
        @Query("soloNoLeidas") soloNoLeidas: Boolean? = null,
    ): NotificationsPageDto
}
