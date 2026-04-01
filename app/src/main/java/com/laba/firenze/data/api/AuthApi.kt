package com.laba.firenze.data.api

import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.Header
import retrofit2.http.POST

/**
 * API per autenticazione OAuth2 (identico a iOS)
 * Base URL: https://logosuni.laba.biz/
 * Endpoint: /identityserver/connect/token
 */
interface AuthApi {

    /**
     * Login con Resource Owner Password Credentials (ROPC)
     * Identico a iOS SessionVM.loginROPC()
     */
    @FormUrlEncoded
    @POST("connect/token")
    suspend fun login(
        @Header("Authorization") basicAuth: String,
        @Field("grant_type") grantType: String = "password",
        @Field("client_id") clientId: String,
        @Field("client_secret") clientSecret: String,
        @Field("scope") scope: String = "LogosUni.Laba.Api offline_access",
        @Field("username") username: String,
        @Field("password") password: String
    ): Response<TokenResponse>

    /**
     * Refresh token (identico a iOS SessionVM.refreshToken())
     */
    @FormUrlEncoded
    @POST("connect/token")
    suspend fun refresh(
        @Header("Authorization") basicAuth: String,
        @Field("grant_type") grantType: String = "refresh_token",
        @Field("client_id") clientId: String,
        @Field("client_secret") clientSecret: String,
        @Field("refresh_token") refreshToken: String
    ): Response<TokenResponse>

}
