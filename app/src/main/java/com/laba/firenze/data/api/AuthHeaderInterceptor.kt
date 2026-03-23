package com.laba.firenze.data.api

import com.laba.firenze.data.local.TokenStore
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Interceptor che aggiunge Authorization: Bearer <access_token> alle richieste API
 * Identico alla logica iOS di aggiunta automatica del token
 */
@Singleton
class AuthHeaderInterceptor @Inject constructor(
    private val tokenStore: TokenStore
) : Interceptor {
    
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        
        // Se la richiesta è per l'endpoint di token, non aggiungere Authorization
        if (originalRequest.url.encodedPath.contains("identityserver/connect/token")) {
            return chain.proceed(originalRequest)
        }
        
        // Supabase usa la propria apikey/Bearer (anon key), non il token LogosUni.
        // Evitare di sovrascrivere l'header Authorization impostato da SupabaseRepository.
        if (originalRequest.url.host?.contains("supabase.co") == true) {
            return chain.proceed(originalRequest)
        }
        
        val accessToken = tokenStore.getCurrentAccessToken()
        
        return if (accessToken.isNotEmpty()) {
            val requestWithAuth = originalRequest.newBuilder()
                .header("Authorization", "Bearer $accessToken")
                .build()
            
            println("🔐 AuthHeaderInterceptor: Adding Bearer token to ${originalRequest.url}")
            chain.proceed(requestWithAuth)
        } else {
            println("🔐 AuthHeaderInterceptor: No token available for ${originalRequest.url}")
            chain.proceed(originalRequest)
        }
    }
}
