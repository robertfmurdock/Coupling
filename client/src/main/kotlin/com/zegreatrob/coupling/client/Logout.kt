package com.zegreatrob.coupling.client


import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import react.dom.div
import react.router.dom.redirect

object Logout : ComponentProvider<PageProps>(), LogoutBuilder

interface LogoutBuilder : ComponentBuilder<PageProps>, GoogleSignIn, LogoutActionDispatcher {

    override fun build(): ReactFunctionComponent<PageProps> = reactFunctionComponent {
        val (isLoggedOut, setIsLoggedOut) = useState(false)
        val (logoutPromise, setLogout) = useState<Any?>(null)
        if (logoutPromise == null) {
            setLogout(
                    GlobalScope.async { waitForLogout(setIsLoggedOut) }
            )
        }

        if (isLoggedOut) {
            redirect(to = "/welcome", from = "")
        } else {
            div { }
        }
    }

    private suspend fun waitForLogout(setIsLoggedOut: (Boolean) -> Unit): Unit = coroutineScope {
        launch { logout() }
        launch { googleSignOut() }
    }.run { setIsLoggedOut(true) }

}
