package com.zegreatrob.coupling.client.user

import com.zegreatrob.coupling.sdk.SdkSyntax
import kotlinext.js.jsObject
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.await
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.yield
import org.w3c.dom.get
import kotlin.browser.window

interface GoogleSignIn : SdkSyntax {

    suspend fun signIn() = getGoogleAuth()
        .performSignIn()
        .createSession()
        .also { window.location.pathname = "/" }

    suspend fun googleSignOut(): Unit = getGoogleAuth()
        .whenLoggedInSignOut()

    suspend fun checkForSignedIn() = coroutineScope {
        waitForIsAuthenticatedToLoad()
        if (window["isAuthenticated"] == true) {
            true
        } else {
            val googleAuth = getGoogleAuth()
            val isSignedIn = googleAuth.isSignedIn.get()

            if (isSignedIn) {
                googleAuth.currentUser.get()
                    .createSession()
            }
            isSignedIn
        }
    }

    private suspend fun waitForIsAuthenticatedToLoad() {
        while (window["isAuthenticated"] === undefined) {
            yield()
        }
    }

    private suspend fun GoogleAuth.whenLoggedInSignOut(): Unit = if (isSignedIn.get()) {
        signOut().await()
    } else Unit

    private suspend fun GoogleUser.createSession(): Unit = with(sdk) {
        createSessionOnCoupling(getAuthResponse().id_token)
            .await()
        Unit
    }

    private suspend fun getGoogleAuth() = loadGoogleAuth2()
        .init(jsObject { client_id = window["googleClientId"] })
        .await()

    private suspend fun loadGoogleAuth2() = CompletableDeferred<GoogleAuth2>()
        .apply {
            gapi.load("auth2") { complete(gapi.auth2) }
        }
        .await()

    private suspend fun GoogleAuth.performSignIn() = if (isSignedIn.get()) {
        currentUser.get()
    } else {
        signIn().await()
    }

    private fun GoogleAuth.signIn() = signIn(
        jsObject {
            scope = "profile email"
            prompt = "consent"
            ux_mode = "redirect"
            redirect_uri = window.location.origin
        }
    )
}
