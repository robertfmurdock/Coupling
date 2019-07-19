package com.zegreatrob.coupling.client


import kotlinx.coroutines.*
import react.RBuilder
import react.RProps
import react.dom.div
import react.router.dom.redirect
import kotlin.js.Promise

data class LogoutProps(val coupling: dynamic) : RProps

interface LogoutRenderer {

    fun RBuilder.logout(props: LogoutProps) = component(logout, props)

    companion object : ReactComponentRenderer, GoogleSignIn {

        private val logout = reactFunctionComponent<LogoutProps> { (coupling) ->
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

        private suspend fun waitForLogout(setIsLoggedOut: (Boolean) -> Unit, coupling: dynamic): Unit = coroutineScope {
            launch { coupling.logout().unsafeCast<Promise<Unit>>().await() }
            launch { signOut() }
        }.run { setIsLoggedOut(true) }

    }

}
