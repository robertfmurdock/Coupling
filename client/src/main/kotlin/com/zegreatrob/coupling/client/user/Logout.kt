package com.zegreatrob.coupling.client.user

import com.zegreatrob.coupling.client.external.react.ComponentBuilder
import com.zegreatrob.coupling.client.external.react.ComponentProvider
import com.zegreatrob.coupling.client.routing.PageProps
import com.zegreatrob.coupling.client.external.react.ReactFunctionComponent
import com.zegreatrob.coupling.client.external.react.reactFunctionComponent
import com.zegreatrob.coupling.client.external.react.useState
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import react.dom.div
import react.router.dom.redirect

object Logout : ComponentProvider<PageProps>(), LogoutBuilder

interface LogoutBuilder : ComponentBuilder<PageProps>, GoogleSignIn, LogoutCommandDispatcher {

    override fun build(): ReactFunctionComponent<PageProps> = reactFunctionComponent {
        val (isLoggedOut, setIsLoggedOut) = useState(false)
        val (logoutPromise, setLogout) = useState<Any?>(null)
        if (logoutPromise == null) {
            setLogout(
                MainScope().launch { waitForLogout(setIsLoggedOut) }
            )
        }

        if (isLoggedOut) {
            redirect(to = "/welcome", from = "")
        } else {
            div { }
        }
    }

    private suspend fun waitForLogout(setIsLoggedOut: (Boolean) -> Unit): Unit = coroutineScope {
        launch { LogoutCommand.perform() }
        launch { googleSignOut() }
    }.run { setIsLoggedOut(true) }

}
