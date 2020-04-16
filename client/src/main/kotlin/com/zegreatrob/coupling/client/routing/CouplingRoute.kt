package com.zegreatrob.coupling.client.routing

import com.zegreatrob.coupling.client.external.react.*
import org.w3c.dom.url.URLSearchParams
import react.RBuilder
import react.RProps
import react.createElement
import react.router.dom.RouteResultProps
import react.router.dom.route
import kotlin.js.Json

object CouplingRoute : RComponent<CouplingRouteProps>(provider()), CouplingRouteBuilder

val RBuilder.couplingRoute get() = CouplingRoute.render(this)

data class CouplingRouteProps(val path: String, val rComponent: RComponent<PageProps>) : RProps

interface CouplingRouteBuilder : SimpleComponentRenderer<CouplingRouteProps> {

    override fun RContext<CouplingRouteProps>.render() = reactElement {
        route<RProps>(props.path, exact = true) { routeProps ->
            createElement(
                props.rComponent.component.rFunction, pageProps(routeProps)
            )
        }
    }

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

}
