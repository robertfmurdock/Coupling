@file:JsModule("@slack/oauth")

package com.zegreatrob.coupling.server.slack.external.oauth

import kotlinx.js.JsPlainObject
import web.http.Request
import web.http.Response
import kotlin.js.Promise

external class InstallProvider(options: InstallProviderOptions) {
    fun handleCallback(request: Request, response: Response)

    fun generateInstallUrl(options: InstallUrlOptions): Promise<String>
    fun handleInstallPath(request: Request, response: Response)
}

@JsPlainObject
sealed external interface InstallUrlOptions {
    val scopes: Array<String>
    val redirectUri: String
}

@JsPlainObject
sealed external interface InstallProviderOptions {
    val clientId: String
    val clientSecret: String
    val stateSecret: String
    val installUrlOptions: InstallUrlOptions
    val legacyStateVerification: Boolean?
    val installationStore: InstallationStore?
}

@JsPlainObject
sealed external interface InstallationStore {
    var storeInstallation: (Installation) -> Unit
    var fetchInstallation: (InstallationQuery) -> Installation
    var deleteInstallation: (InstallationQuery) -> Unit
}

@JsPlainObject
sealed external interface InstallationQuery {
    val teamId: String
    val enterpriseId: String
    val userId: String
    val conversationId: String
    val isEnterpriseInstall: Boolean
}

@JsPlainObject
sealed external interface Installation {
    val botToken: String?
    val userToken: String?
    val botId: String?
    val botUserId: String?
    val teamId: String?
    val enterpriseId: String?
}
