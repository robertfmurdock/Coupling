package com.zegreatrob.coupling.client.user

import com.zegreatrob.coupling.client.CommandDispatcher
import com.zegreatrob.coupling.client.routing.PageProps
import com.zegreatrob.coupling.client.reactFunction
import com.zegreatrob.react.dataloader.useScope
import kotlinx.browser.window
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import org.w3c.dom.get
import org.w3c.dom.url.URL
import react.dom.div
import react.useState

val Logout = reactFunction<PageProps> { props ->
    val scope = useScope("Logout")
    var isLoggedOut by useState(false)
    val (logoutPromise, setLogout) = useState<Any?>(null)
    if (logoutPromise == null) {
        setLogout(
            scope.launch { props.commander.runQuery { waitForLogout { isLoggedOut = it } } }
        )
    }
    if (isLoggedOut) {
        window.location.assign("$authLogoutUrl")
    } else {
        div { }
    }
}

private val authLogoutUrl: URL
    get() = URL("https://${window["auth0Domain"]}/v2/logout").apply {
        searchParams.apply {
            append("client_id", window["auth0ClientId"].toString())
            append("returnTo", URL(window.location.toString()).origin)
        }
    }

private suspend fun CommandDispatcher.waitForLogout(setIsLoggedOut: (Boolean) -> Unit): Unit = coroutineScope {
    launch { LogoutCommand.perform() }
}.run { setIsLoggedOut(true) }
