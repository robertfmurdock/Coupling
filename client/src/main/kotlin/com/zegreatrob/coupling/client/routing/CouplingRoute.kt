package com.zegreatrob.coupling.client.routing

import com.zegreatrob.coupling.client.components.external.auth0.react.useAuth0Data
import com.zegreatrob.minreact.nfc
import kotlinx.browser.window
import react.ChildrenBuilder
import react.FC
import react.Props
import react.create
import react.router.NavigateFunction
import react.router.PathRoute
import react.router.dom.useSearchParams
import react.router.useNavigate
import react.router.useParams

fun ChildrenBuilder.couplingRoute(path: String, rComponent: FC<PageProps>) = PathRoute {
    this.path = path
    this.element = CouplingRoute.create { this.rComponent = rComponent }
}

external interface CouplingRouteProps : Props {
    var rComponent: FC<PageProps>
}

val CouplingRoute by nfc<CouplingRouteProps> {
    val (searchParams) = useSearchParams()
    val params = useParams()
    val navigate = useNavigate()

    val auth0Data = useAuth0Data()

    it.rComponent {
        pathParams = params
        search = searchParams
        commander = MasterCommander(auth0Data.getAccessTokenSilently)
    }
    window.asDynamic().pathSetter = newPathSetter(navigate)
}

private fun newPathSetter(navigate: NavigateFunction) = { path: String ->
    navigate.invoke(
        if (path.startsWith("/")) {
            path
        } else {
            "/$path"
        },
    )
}
