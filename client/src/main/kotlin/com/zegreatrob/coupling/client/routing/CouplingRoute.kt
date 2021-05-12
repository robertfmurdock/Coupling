package com.zegreatrob.coupling.client.routing

import kotlinx.browser.window
import org.w3c.dom.url.URLSearchParams
import react.RBuilder
import react.RClass
import react.RProps
import react.createElement
import react.router.dom.RouteResultProps
import react.router.dom.route
import kotlin.js.Json

fun RBuilder.couplingRoute(path: String, rComponent: RClass<PageProps>) =
    route<RProps>(path, exact = true) { routeProps ->
        createElement(rComponent, pageProps(routeProps))
            .also { window.asDynamic().pathSetter = newPathSetter(routeProps) }
    }

private fun pageProps(routeProps: RouteResultProps<RProps>) = PageProps(
    pathParams = routeProps.pathParams(),
    search = URLSearchParams(routeProps.location.search),
    commander = MasterCommander
)

private fun newPathSetter(routeProps: RouteResultProps<RProps>) = { path: String -> routeProps.history.push(path) }

private fun RouteResultProps<RProps>.pathParams(): Map<String, String> {
    val paramsJson = match.params.unsafeCast<Json>()

    return js("Object").keys(paramsJson).unsafeCast<Array<String>>()
        .map { key -> key to paramsJson[key].unsafeCast<String>() }
        .toMap()
}
