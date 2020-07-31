package com.zegreatrob.coupling.client.user

import com.zegreatrob.coupling.client.CommandDispatcher
import com.zegreatrob.coupling.client.routing.PageProps
import com.zegreatrob.minreact.reactFunction
import com.zegreatrob.react.dataloader.useScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import react.dom.div
import react.router.dom.redirect
import react.useState

val Logout = reactFunction<PageProps> { props ->
    val scope = useScope("Logout")
    val (isLoggedOut, setIsLoggedOut) = useState(false)
    val (logoutPromise, setLogout) = useState<Any?>(null)
    if (logoutPromise == null) {
        setLogout(
            scope.launch { props.commander.runQuery { waitForLogout(setIsLoggedOut) } }
        )
    }
    if (isLoggedOut) {
        redirect(to = "/welcome", from = "")
    } else {
        div { }
    }
}

private suspend fun CommandDispatcher.waitForLogout(setIsLoggedOut: (Boolean) -> Unit): Unit = coroutineScope {
    launch { LogoutCommand.perform() }
    launch { googleSignOut() }
}.run { setIsLoggedOut(true) }
