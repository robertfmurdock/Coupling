package com.zegreatrob.coupling.client.routing

import kotlinx.browser.window
import react.FC
import react.Props
import react.RBuilder
import react.createElement
import react.router.NavigateFunction
import react.router.Route
import react.router.dom.useSearchParams
import react.router.useNavigate
import react.router.useParams

fun RBuilder.couplingRoute(path: String, rComponent: FC<PageProps>) = Route {
    attrs.path = path
    attrs.element = createElement { CouplingRoute { attrs.rComponent = rComponent } }
}

external interface CouplingRouteProps : Props {
    var rComponent: FC<PageProps>
}

val CouplingRoute = FC<CouplingRouteProps> {
    val (searchParams) = useSearchParams()
    val params = useParams()
    val navigate = useNavigate()

    it.rComponent {
        pathParams = params
        search = searchParams
        commander = MasterCommander
    }
    window.asDynamic().pathSetter = newPathSetter(navigate)
}

private fun newPathSetter(navigate: NavigateFunction) = { path: String ->
    navigate.invoke(
        if (path.startsWith("/"))
            path
        else
            "/$path"
    )
}
