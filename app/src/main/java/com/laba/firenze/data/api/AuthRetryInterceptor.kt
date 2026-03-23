package com.laba.firenze.data.api

import com.laba.firenze.data.repository.SessionRepository
import dagger.Lazy
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Interceptor che su 401 prova restore forte + retry (identico a iOS withTokenRetry).
 * Se riceve 401 da API LogosUni (non identityserver), prova restoreSessionStrong e ritenta una volta.
 * Usa Lazy per evitare dipendenza circolare con OkHttpClient.
 */
@Singleton
class AuthRetryInterceptor @Inject constructor(
    private val sessionRepository: Lazy<SessionRepository>
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()

        // Non fare retry per identityserver (login/token)
        if (request.url.encodedPath.contains("identityserver")) {
            return chain.proceed(request)
        }

        // Se è già un retry, non ritentare
        if (request.tag(RetryTag::class.java) != null) {
            return chain.proceed(request)
        }

        val response = chain.proceed(request)

        if (response.code != 401) {
            return response
        }

        // Restore forte (identico a iOS) e ritenta una sola volta
        val restored = runBlocking {
            sessionRepository.get().restoreSessionStrong(force = true)
        }

        if (!restored) {
            // Restore fallito: ritorna 401 (il caller farà logout)
            return response
        }

        response.close()

        val retryRequest = request.newBuilder()
            .tag(RetryTag::class.java, RetryTag)
            .build()

        return chain.proceed(retryRequest)
    }

    private object RetryTag
}
