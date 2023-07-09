package com.zegreatrob.coupling.client.routing

import com.zegreatrob.coupling.client.components.external.auth0.react.useAuth0Data
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

fun couplingRoute(path: String, title: String, rComponent: FC<PageProps>) = jso<RouteObject> {
    this.path = path
    this.element = CouplingRoute.create {
        this.title = title
        this.rComponent = rComponent
    }
}

external interface CouplingRouteProps : Props {
    var rComponent: FC<PageProps>
    var title: String
}

val CouplingRoute by nfc<CouplingRouteProps> { props ->
    val (searchParams) = useSearchParams()
    val params = useParams()
    val navigate = useNavigate()

    val auth0Data = useAuth0Data()
    val title = props.title
    useEffect(title) {
        window.document.title = "${appTitle()} - $title"
    }

    props.rComponent {
        pathParams = params
        search = searchParams
        commander = MasterCommander {
            runCatching { auth0Data.getAccessTokenSilently() }
                .onFailure { auth0Data.loginWithRedirect(jso()) }
                .getOrThrow()
        }
    }
    window.asDynamic().pathSetter = newPathSetter(navigate)
}

private fun appTitle() = window.document.title.split("-")[0].trim()

private fun newPathSetter(navigate: NavigateFunction) = { path: String ->
    navigate.invoke(
        if (path.startsWith("/")) {
            path
        } else {
            "/$path"
        },
    )
}
