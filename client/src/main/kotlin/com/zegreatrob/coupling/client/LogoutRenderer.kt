package com.zegreatrob.coupling.client

import kotlinx.coroutines.Deferred
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.asDeferred
import kotlinx.coroutines.async
import react.RProps
import react.dom.div
import react.router.dom.redirect
import kotlin.js.Promise

interface LogoutRenderer : ReactComponentRenderer, GoogleSignIn {

    val logout
        get() = rFunction<LogoutRendererProps> { (coupling) ->
            val (isLoggedOut, setIsLoggedOut) = useState(false)
            val (logoutPromise, setLogout) = useState<Any?>(null)
            if (logoutPromise == null) {
                setLogout(
                        GlobalScope.async { waitForLogout(setIsLoggedOut, coupling) }
                )
            }

            if (isLoggedOut) {
                redirect(to = "/welcome", from = "")
            } else {
                div { }
            }
        }

    private suspend fun waitForLogout(setIsLoggedOut: (Boolean) -> Unit, coupling: dynamic) {
        Pair(
                coupling.logout().unsafeCast<Promise<Unit>>().asDeferred(),
                GlobalScope.async { signOut() }
        ).await()

        setIsLoggedOut(true)
    }

}

private suspend fun <A, B> Pair<Deferred<A>, Deferred<B>>.await() = Pair(
        first.await(),
        second.await()
)

data class LogoutRendererProps(val coupling: dynamic) : RProps