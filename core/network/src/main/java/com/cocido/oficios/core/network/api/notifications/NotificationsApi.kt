package com.cocido.oficios.core.network.api.notifications

import com.cocido.oficios.core.network.api.notifications.dto.RegisterDeviceTokenRequestDto
import kotlinx.serialization.json.JsonElement
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface NotificationsApi {

    @POST("notifications/device-token")
    suspend fun registerDeviceToken(
        @Body request: RegisterDeviceTokenRequestDto,
    ): JsonElement

    @PATCH("notifications/{id}/read")
    suspend fun markAsRead(
        @Path("id") notificationId: Long,
    ): JsonElement

    @GET("notifications")
    suspend fun getNotifications(
        @Query("pageSize") pageSize: Int = 20,
        @Query("pageNumber") pageNumber: Int = 1,
    ): JsonElement
}
