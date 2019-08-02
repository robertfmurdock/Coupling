package com.zegreatrob.coupling.client.routing

import com.zegreatrob.coupling.client.*
import org.w3c.dom.url.URLSearchParams
import react.RBuilder
import react.RClass
import react.RProps
import react.ReactElement
import react.router.dom.RouteResultProps
import react.router.dom.route
import kotlin.js.Json

@JsModule("ServiceContext")
@JsNonModule
private external val serviceContextModule: dynamic


object CouplingRoute : ComponentProvider<CouplingRouteProps>(), CouplingRouteBuilder

val RBuilder.couplingRoute get() = CouplingRoute.captor(this)

data class CouplingRouteProps(val path: String, val component: RClass<PageProps>) : RProps

@JsModule("react")
@JsNonModule
private external val React: dynamic

interface CouplingRouteBuilder : ComponentBuilder<CouplingRouteProps> {

    private val serviceContextConsumer get() = serviceContextModule.default.Consumer.unsafeCast<Any>()

    override fun build() = reactFunctionComponent<CouplingRouteProps> { props ->
        componentWithFunctionChildren(serviceContextConsumer) { coupling: Coupling ->
            route<RProps>(props.path, exact = true) { routeProps ->
                React.createElement(props.component, pageProps(coupling, routeProps))
                        .unsafeCast<ReactElement>()
            }
        }
    }

    private fun pageProps(coupling: Coupling, routeProps: RouteResultProps<RProps>) = PageProps(
            coupling = coupling,
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
