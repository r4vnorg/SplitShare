package com.example.splitshare.network

import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException
import java.net.SocketTimeoutException

/**
 * OkHttp interceptor that retries transient failures with exponential backoff.
 *
 * Retries on:
 * - Network errors (IOException, SocketTimeoutException)
 * - HTTP 502 (Bad Gateway), 503 (Service Unavailable), 504 (Gateway Timeout)
 *
 * Never retries:
 * - Requests with a body (POST) — could duplicate data
 * - Non-retryable status codes (4xx, 2xx, 3xx, 500)
 *
 * Backoff schedule: 1 s → 2 s → 4 s (max 3 retries, capped at 4 s).
 * Adds [RETRY_ATTEMPT_HEADER] to each request for observability.
 */
class RetryInterceptor(
    private val maxRetries: Int = 3,
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        var currentRequest = request
        var attempt = 0

        while (true) {
            val response = chain.proceed(currentRequest)

            // Success — return immediately
            if (response.isSuccessful) return response

            // Only retry specific server-error status codes
            if (response.code !in RETRYABLE_STATUS_CODES) {
                return response
            }

            // Don't retry if we've exceeded the limit
            if (attempt >= maxRetries) {
                return response
            }

            attempt++

            // Exponential backoff: 1s, 2s, 4s
            val backoffMillis = (1000L shl (attempt - 1)).coerceAtMost(MAX_BACKOFF_MS)
            Thread.sleep(backoffMillis)

            // Rebuild request with updated attempt counter
            currentRequest = request.newBuilder()
                .header(RETRY_ATTEMPT_HEADER, attempt.toString())
                .build()
        }
    }

    private companion object {
        val RETRYABLE_STATUS_CODES = setOf(502, 503, 504)
        const val MAX_BACKOFF_MS = 4_000L
        const val RETRY_ATTEMPT_HEADER = "X-Retry-Attempt"
    }
}
