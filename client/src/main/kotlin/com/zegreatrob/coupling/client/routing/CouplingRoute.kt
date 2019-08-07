package com.zegreatrob.coupling.client.routing

import com.zegreatrob.coupling.client.external.react.ComponentBuilder
import com.zegreatrob.coupling.client.external.react.ComponentProvider
import com.zegreatrob.coupling.client.external.react.reactFunctionComponent
import org.w3c.dom.url.URLSearchParams
import react.RBuilder
import react.RProps
import react.createElement
import react.router.dom.RouteResultProps
import react.router.dom.route
import kotlin.js.Json

object CouplingRoute : ComponentProvider<CouplingRouteProps>(), CouplingRouteBuilder

val RBuilder.couplingRoute get() = CouplingRoute.captor(this)

data class CouplingRouteProps(val path: String, val componentProvider: ComponentProvider<PageProps>) : RProps

interface CouplingRouteBuilder : ComponentBuilder<CouplingRouteProps> {

    override fun build() = reactFunctionComponent<CouplingRouteProps> { props ->
        route<RProps>(props.path, exact = true) { routeProps ->
            createElement(
                    props.componentProvider.component.rFunction, pageProps(routeProps)
            )
        }
    }

    private fun pageProps(routeProps: RouteResultProps<RProps>) = PageProps(
            pathParams = routeProps.pathParams(),
            pathSetter = { path -> routeProps.history.push(path) },
            search = URLSearchParams(routeProps.location.search)
    )

    private fun RouteResultProps<RProps>.pathParams(): Map<String, String> {
        val paramsJson = match.params.unsafeCast<Json>()

        return js("Object").keys(paramsJson).unsafeCast<Array<String>>()
                .map { key -> key to paramsJson[key].unsafeCast<String>() }
                .toMap()
    }

}
