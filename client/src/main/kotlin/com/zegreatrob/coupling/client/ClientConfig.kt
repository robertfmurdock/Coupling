package com.zegreatrob.coupling.client

data class ClientConfig(
    val prereleaseMode: Boolean,
    val auth0ClientId: String,
    val auth0Domain: String,
    val basename: String,
    val expressEnv: String,
    val webpackPublicPath: String,
    val websocketHost: String
)
