package com.zegreatrob.coupling.client.routing

import com.zegreatrob.minreact.DataPropsBridge
import com.zegreatrob.minreact.TMFC
import kotlinx.browser.window
import org.w3c.dom.url.URLSearchParams
import react.Props
import react.RBuilder
import react.createElement
import react.fc
import react.router.*
import react.router.dom.useSearchParams

fun RBuilder.couplingRoute(path: String, rComponent: TMFC<PageProps>) = Route {
    attrs.path = path
    attrs.element = createElement { CouplingRoute { attrs.rComponent = rComponent } }
}

external interface CouplingRouteProps : Props {
    var rComponent: TMFC<PageProps>
}

val CouplingRoute = fc<CouplingRouteProps> {
    val (searchParams) = useSearchParams()
    val params = useParams()
    val navigate = useNavigate()
    child(createElement(it.rComponent, pageProps(params, searchParams).unsafeCast<DataPropsBridge<PageProps>>()))
        .also { window.asDynamic().pathSetter = newPathSetter(navigate) }
}

private fun pageProps(routeProps: Params, search: URLSearchParams) = PageProps(
    pathParams = routeProps,
    search = search,
    commander = MasterCommander
)

private fun newPathSetter(navigate: NavigateFunction) = { path: String ->
    navigate.invoke(
        if (path.startsWith("/"))
            path
        else
            "/$path"
    )
}
