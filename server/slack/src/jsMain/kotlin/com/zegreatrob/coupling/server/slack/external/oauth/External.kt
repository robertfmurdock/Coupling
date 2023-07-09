@file:JsModule("@slack/oauth")

package com.zegreatrob.coupling.server.slack.external.oauth

import web.http.Request
import web.http.Response
import kotlin.js.Promise

external class InstallProvider(options: InstallProviderOptions) {
    fun handleCallback(request: Request, response: Response)

    fun generateInstallUrl(options: InstallUrlOptions): Promise<String>
    fun handleInstallPath(request: Request, response: Response)
}

external interface InstallUrlOptions {
    var scopes: Array<String>
    var redirectUri: String
}

external interface InstallProviderOptions {
    var clientId: String
    var clientSecret: String
    var stateSecret: String
    var legacyStateVerification: Boolean
    var installationStore: InstallationStore
    var installUrlOptions: InstallUrlOptions
}

external interface InstallationStore {
    var storeInstallation: (Installation) -> Unit
    var fetchInstallation: (InstallationQuery) -> Installation
    var deleteInstallation: (InstallationQuery) -> Unit
}

external interface InstallationQuery {
    var teamId: String
    var enterpriseId: String
    var userId: String
    var conversationId: String
    var isEnterpriseInstall: Boolean
}

external interface Installation {
    var botToken: String?
    var userToken: String?
    var botId: String?
    var botUserId: String?
    var teamId: String?
    var enterpriseId: String?
}
