package com.zegreatrob.coupling.client.routing

import com.zegreatrob.coupling.client.components.external.auth0.react.useAuth0Data
import com.zegreatrob.coupling.client.external.react.ga.ReactGA
import com.zegreatrob.minreact.nfc
import js.core.jso
import kotlinx.browser.window
import react.FC
import react.Props
import react.create
import react.router.NavigateFunction
import react.router.RouteObject
import react.router.dom.useSearchParams
import react.router.useNavigate
import react.router.useParams
import react.useEffect

fun couplingRoute(path: String, rComponent: FC<PageProps>) = jso<RouteObject> {
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

    useEffect {
        ReactGA.pageview(window.location.pathname + window.location.search)
    }

    it.rComponent {
        pathParams = params
        search = searchParams
        commander = MasterCommander {
            runCatching { auth0Data.getAccessTokenSilently() }
                .onFailure { auth0Data.loginWithRedirect() }
                .getOrThrow()
        }
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
