package com.zegreatrob.coupling.client

import kotlinext.js.jsObject
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.await
import org.w3c.dom.get
import kotlin.browser.window
import kotlin.js.json

interface GoogleSignIn {

    suspend fun signIn() {
        val googleAuth = getGoogleAuth()
        val user = performSignIn(googleAuth)
        createSession(user)
        window.location.pathname = "/"
    }

    private suspend fun createSession(user: GoogleUser) {
        val idToken = user.getAuthResponse().id_token
        axios.post("/auth/google-token", json("idToken" to idToken))
                .await()
    }

    private suspend fun getGoogleAuth(): GoogleAuth {
        val auth2 = loadGoogleAuth2()

        return auth2.init(jsObject { client_id = window["googleClientId"] })
                .await()
    }

    private suspend fun loadGoogleAuth2() = CompletableDeferred<GoogleAuth2>()
            .apply {
                gapi.load("auth2") {
                    complete(gapi.auth2)
                }
            }
            .await()

    private suspend fun performSignIn(googleAuth: GoogleAuth): GoogleUser {
        val isSignedIn = googleAuth.isSignedIn.get().unsafeCast<Boolean>()
        return if (isSignedIn) {
            googleAuth.currentUser.get()
        } else {
            val origin = window.location.origin

            googleAuth.signIn(jsObject {
                scope = "profile email"
                prompt = "consent"
                ux_mode = "redirect"
                redirect_uri = origin
            })
                    .await()
        }
    }

}
