package com.cocido.oficios.core.network.session

import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response

class TokenRefreshCoordinatorTest {

    @Test
    fun `network error on refresh does not clear the session`() {
        assertFalse(shouldClearSessionAfterRefresh(UnknownHostException("offline")))
        assertFalse(shouldClearSessionAfterRefresh(IOException("reset")))
        assertFalse(shouldClearSessionAfterRefresh(SocketTimeoutException("timeout")))
        assertFalse(shouldClearSessionAfterRefresh(http(500)))
    }

    @Test
    fun `refresh 401 and 403 clear the session`() {
        assertTrue(shouldClearSessionAfterRefresh(http(401)))
        assertTrue(shouldClearSessionAfterRefresh(http(403)))
    }

    private fun http(code: Int): HttpException = HttpException(
        Response.error<String>(code, "".toResponseBody("text/plain".toMediaType())),
    )
}
