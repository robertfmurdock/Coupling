package com.zegreatrob.coupling.client.routing

import com.zegreatrob.coupling.client.external.react.RComponent
import com.zegreatrob.coupling.client.external.react.reactFunction
import org.w3c.dom.url.URLSearchParams
import react.RBuilder
import react.RProps
import react.createElement
import react.router.dom.RouteResultProps
import react.router.dom.route
import kotlin.js.Json

data class CouplingRouteProps(val path: String, val rComponent: RComponent<PageProps>) : RProps

val CouplingRoute = reactFunction<CouplingRouteProps> { props ->
    route<RProps>(props.path, exact = true) { routeProps ->
        createElement(
            props.rComponent.component.rFunction, pageProps(routeProps)
        )
    }
}

fun RBuilder.couplingRoute(path: String, rComponent: RComponent<PageProps>) = CouplingRoute.render(this)(
    CouplingRouteProps(path, rComponent)
)

private fun pageProps(routeProps: RouteResultProps<RProps>) = PageProps(
    pathParams = routeProps.pathParams(),
    pathSetter = { path -> routeProps.history.push(path) },
    search = URLSearchParams(routeProps.location.search),
    commander = MasterCommander
)

private fun RouteResultProps<RProps>.pathParams(): Map<String, String> {
    val paramsJson = match.params.unsafeCast<Json>()

    return js("Object").keys(paramsJson).unsafeCast<Array<String>>()
        .map { key -> key to paramsJson[key].unsafeCast<String>() }
        .toMap()
}
