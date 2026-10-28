package com.cocido.oficios.core.data.repository

import com.cocido.oficios.core.common.result.AppError
import com.cocido.oficios.core.common.result.AppResult
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DeleteOnlyAfterCreateSucceedsTest {

    @Test
    fun `post 400 does not call delete`() = runTest {
        var deleted = false
        val result = deleteOnlyAfterCreateSucceeds(
            created = AppResult.Error(AppError.Validation("Datos de entrada no válidos.", backendCode = "VA01")),
        ) {
            deleted = true
            AppResult.Success(Unit)
        }
        assertTrue(result is AppResult.Error)
        assertEquals("VA01", (result as AppResult.Error).error.backendCode)
        assertFalse(deleted)
    }

    @Test
    fun `post 201 deletes the previous rubro`() = runTest {
        var deleted = false
        val result = deleteOnlyAfterCreateSucceeds(created = AppResult.Success(Unit)) {
            deleted = true
            AppResult.Success(Unit)
        }
        assertTrue(result is AppResult.Success)
        assertTrue(deleted)
    }
}
