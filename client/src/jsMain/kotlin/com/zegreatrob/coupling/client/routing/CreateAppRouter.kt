package com.zegreatrob.coupling.client.routing

import com.zegreatrob.coupling.client.AboutRoute
import com.zegreatrob.coupling.client.demo.DemoRoute
import com.zegreatrob.coupling.client.demo.LoadingRoute
import com.zegreatrob.coupling.client.external.tanstack.react.router.redirect
import com.zegreatrob.coupling.client.party.PartyListRoute
import com.zegreatrob.coupling.client.welcome.WelcomeRoute
import js.objects.recordOf
import js.objects.unsafeJso
import react.FC
import react.Props
import tanstack.react.router.RootRoute
import tanstack.react.router.Route
import tanstack.react.router.RouteOptions
import tanstack.react.router.Router
import tanstack.react.router.RouterOptions
import tanstack.react.router.createRootRoute
import tanstack.react.router.createRoute
import tanstack.react.router.createRouter
import tanstack.router.core.RoutePath

fun createAppRouter(): Router {
    val rootRoute = createRootRoute()
    rootRoute.addChildren(
        arrayOf(
            couplingRoute("/welcome", WelcomeRoute, rootRoute),
            couplingRoute("/about", AboutRoute, rootRoute),
            couplingRoute("/demo", DemoRoute, rootRoute),
            couplingRoute("/loading", LoadingRoute, rootRoute),
            authorizedRoute("/parties/", PartyListRoute, rootRoute),
        ),
    )

    return createRouter(
        options = RouterOptions(
            routeTree = rootRoute,
        ),
    )
}

private fun couplingRoute(path: String, component: FC<Props>, rootRoute: RootRoute): Route = createRoute(
    options = RouteOptions(
        getParentRoute = { rootRoute },
        path = RoutePath(path),
        component = component,
    ),
)

private fun authorizedRoute(path: String, component: FC<Props>, rootRoute: RootRoute): Route = createRoute(
    options = RouteOptions(
        getParentRoute = { rootRoute },
        path = RoutePath(path),
        component = component,
        beforeLoad = { options ->
            if (!options.context.asDynamic()["isSignedIn"]) {
                val location = options.location

                throw redirect(
                    unsafeJso {
                        to = RoutePath("/welcome")
                        search = recordOf("path" to "${location.pathname}?${location.search}")
                    },
                )
            }

            null
        },
    ),
)
