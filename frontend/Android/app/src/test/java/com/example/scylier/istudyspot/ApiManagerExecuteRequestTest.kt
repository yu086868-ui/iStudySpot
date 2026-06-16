package com.example.scylier.istudyspot

import com.example.scylier.istudyspot.infra.network.ApiManager
import com.example.scylier.istudyspot.infra.network.ApiService
import com.example.scylier.istudyspot.models.ApiResponse
import com.example.scylier.istudyspot.models.BaseResponse
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.Response
import java.io.IOException
import java.net.SocketTimeoutException

class ApiManagerExecuteRequestTest {

    private val apiService = mockk<ApiService>(relaxed = true)
    private val apiManager = ApiManager(apiService = apiService, useMockData = false)

    @Test
    fun executeRequestShouldHandleSuccessBusinessErrorAndEmptyBody() = runTest {
        val success = apiManager.executeRequest {
            Response.success(BaseResponse(code = 200, message = "ok", data = "value"))
        }
        assertEquals("value", (success as ApiResponse.Success).data)

        val businessError = apiManager.executeRequest {
            Response.success(BaseResponse<String>(code = 400, message = "bad", data = null))
        }
        assertEquals("bad", (businessError as ApiResponse.Error).message)

        val empty = apiManager.executeRequest<String> {
            Response.success(null)
        }
        assertEquals(500, (empty as ApiResponse.Error).code)
    }

    @Test
    fun executeRequestShouldHandleHttpErrorsAndExceptions() = runTest {
        val jsonError = apiManager.executeRequest<String> {
            Response.error(
                422,
                """{"message":"invalid"}""".toResponseBody("application/json".toMediaType())
            )
        }
        assertEquals(500, (jsonError as ApiResponse.Error).code)
        assertTrue(jsonError.message.isNotBlank())

        val htmlError = apiManager.executeRequest<String> {
            Response.error(
                500,
                "<html>server down</html>".toResponseBody("text/html".toMediaType())
            )
        }
        assertEquals("Server processing error", (htmlError as ApiResponse.Error).message)

        val timeout = apiManager.executeRequest<String> {
            throw SocketTimeoutException("slow")
        }
        assertEquals(408, (timeout as ApiResponse.Error).code)

        val io = apiManager.executeRequest<String> {
            throw IOException("offline")
        }
        assertEquals(408, (io as ApiResponse.Error).code)

        val generic = apiManager.executeRequest<String> {
            throw IllegalStateException("boom")
        }
        assertEquals(500, (generic as ApiResponse.Error).code)
    }

    @Test
    fun executeRawRequestShouldHandleSuccessHttpErrorAndExceptions() = runTest {
        val success = apiManager.executeRawRequest {
            Response.success("raw")
        }
        assertEquals("raw", (success as ApiResponse.Success).data)

        val empty = apiManager.executeRawRequest<String> {
            Response.success(null)
        }
        assertEquals(500, (empty as ApiResponse.Error).code)

        val error = apiManager.executeRawRequest<String> {
            Response.error(
                404,
                """{"error":"missing"}""".toResponseBody("application/json".toMediaType())
            )
        }
        assertEquals(500, (error as ApiResponse.Error).code)
        assertTrue(error.message.isNotBlank())

        val generic = apiManager.executeRawRequest<String> {
            throw IllegalArgumentException("broken")
        }
        assertTrue((generic as ApiResponse.Error).message.contains("broken"))
    }
}
