package com.zegreatrob.coupling.client.routing

import react.FC
import react.Props
import react.router.Navigate
import react.router.useLocation
import web.url.URLSearchParams

val RedirectUnauthenticated = FC<Props> {
    val location = useLocation()
    val params = URLSearchParams(arrayOf()).apply { append("path", "${location.pathname}?${location.search}") }
    Navigate { to = "/welcome?$params" }
}
