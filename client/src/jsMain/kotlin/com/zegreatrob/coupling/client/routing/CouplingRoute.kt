package com.zegreatrob.coupling.client.routing

import com.zegreatrob.coupling.client.ClientConfig
import com.zegreatrob.coupling.client.components.external.auth0.react.useAuth0Data
import com.zegreatrob.minreact.ReactFunc
import com.zegreatrob.minreact.nfc
import js.objects.unsafeJso
import kotlinx.browser.window
import react.FC
import react.Props
import react.useEffect
import tanstack.react.router.useNavigate
import tanstack.react.router.useParams
import tanstack.react.router.useSearch
import tanstack.router.core.RoutePath
import tanstack.router.core.UseNavigateResult

external interface CouplingRouteProps : Props {
    var rComponent: FC<PageProps>
    var title: String
    var config: ClientConfig
}

@ReactFunc
val CouplingRoute by nfc<CouplingRouteProps> { props ->
    val searchParams = useSearch()
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
                .onFailure { auth0Data.loginWithRedirect(unsafeJso()) }
                .getOrThrow()
        }
        config = props.config
    }
    window.asDynamic().pathSetter = newPathSetter(navigate)
}

private fun appTitle() = window.document.title.split("-")[0].trim()

private fun newPathSetter(navigate: UseNavigateResult) = { path: String ->

    navigate(
        unsafeJso {
            to = RoutePath(
                if (path.startsWith("/")) {
                    path
                } else {
                    "/$path"
                },
            )
        },
    )
}
