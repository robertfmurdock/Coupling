package com.zegreatrob.coupling.client.routing

import com.zegreatrob.coupling.client.AboutPage
import com.zegreatrob.coupling.client.ClientConfig
import com.zegreatrob.coupling.client.components.animationsDisabledContext
import com.zegreatrob.coupling.client.components.external.auth0.react.useAuth0Data
import com.zegreatrob.coupling.client.welcome.WelcomePage
import com.zegreatrob.minreact.DataPropsBind
import com.zegreatrob.minreact.ntmFC
import js.core.jso
import react.create
import react.router.RouteObject
import react.router.RouterProvider
import react.router.dom.createBrowserRouter
import react.useMemo

data class CouplingRouter(val animationsDisabled: Boolean, val config: ClientConfig) :
    DataPropsBind<CouplingRouter>(couplingRouter)

val couplingRouter by ntmFC<CouplingRouter> { (animationsDisabled, config) ->
    val (_, isSignedIn, isLoading) = useAuth0Data()
    val browserRouter = useMemo(isSignedIn, config) {
        createBrowserRouter(
            routes = arrayOf(
                couplingRoute("/welcome/", "Welcome", WelcomePage),
                couplingRoute("/about", "About", AboutPage),
            ).plus(routes()),
            opts = jso { basename = config.basename },
        )
    }
    animationsDisabledContext.Provider(animationsDisabled) {
        if (!isLoading) {
            RouterProvider {
                router = browserRouter
            }
        }
    }
}

private fun routes() = (
    arrayOf(redirectUnauthenticated())
    )
    .plus(jso<RouteObject> { element = LostRoute.create() })

private fun redirectUnauthenticated(): RouteObject = jso {
    path = "*"
    element = RedirectUnauthenticated.create()
}
