package com.zegreatrob.coupling.client.routing

import js.objects.unsafeJso
import react.FC
import react.Props
import react.useEffect
import tanstack.react.router.useLocation
import tanstack.react.router.useNavigate
import tanstack.router.core.RoutePath
import web.url.URLSearchParams

val RedirectUnauthenticated = FC<Props> {
    val location = useLocation()
    val params = URLSearchParams(arrayOf()).apply { append("path", "${location.pathname}?${location.search}") }

    val navigate = useNavigate()

    useEffect {
        navigate(unsafeJso { to = RoutePath("/welcome?$params") })
    }
}
