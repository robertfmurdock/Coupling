package com.zegreatrob.coupling.client.components

import com.zegreatrob.minreact.nfc
import react.FC
import react.PropsWithChildren
import react.useMemo
import tanstack.history.CreateMemoryHistoryOpts
import tanstack.history.createMemoryHistory
import tanstack.react.router.RouteOptions
import tanstack.react.router.RouterOptions
import tanstack.react.router.RouterProvider
import tanstack.react.router.createRootRoute
import tanstack.react.router.createRoute
import tanstack.react.router.createRouter
import tanstack.router.core.RoutePath

val TestRouter by nfc<PropsWithChildren> { props ->
    val defaultComponent = useMemo<react.ComponentType<react.Props>> {
        FC { +props.children }
    }
    RouterProvider {
        router = createRouter(
            options = RouterOptions(
                routeTree = createRootRoute().also {
                    it.addChildren(
                        arrayOf(
                            createRoute(
                                RouteOptions(
                                    path = RoutePath("/"),
                                    getParentRoute = { it }, component = defaultComponent
                                )
                            )
                        )
                    )
                },
                history = createMemoryHistory(CreateMemoryHistoryOpts(initialEntries = arrayOf("/"), initialIndex = 0)),
            ),
        )
    }
}
