package com.zegreatrob.coupling.client.user

import com.zegreatrob.coupling.client.external.react.*
import com.zegreatrob.coupling.client.routing.PageProps
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import react.ReactElement
import react.dom.div
import react.router.dom.redirect

object Logout : RComponent<PageProps>(provider()), LogoutBuilder

interface LogoutBuilder : SimpleComponentRenderer<PageProps>, GoogleSignIn, LogoutCommandDispatcher {

    override fun RContext<PageProps>.render(): ReactElement {
        val (isLoggedOut, setIsLoggedOut) = useState(false)
        val (logoutPromise, setLogout) = useState<Any?>(null)
        if (logoutPromise == null) {
            setLogout(
                MainScope().launch { waitForLogout(setIsLoggedOut) }
            )
        }
        return reactElement {
            if (isLoggedOut) {
                redirect(to = "/welcome", from = "")
            } else {
                div { }
            }
        }
    }

    private suspend fun waitForLogout(setIsLoggedOut: (Boolean) -> Unit): Unit = coroutineScope {
        launch { LogoutCommand.perform() }
        launch { googleSignOut() }
    }.run { setIsLoggedOut(true) }

}
