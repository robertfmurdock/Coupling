package com.zegreatrob.coupling.client.components

import com.zegreatrob.minreact.nfc
import js.objects.jso
import react.Fragment
import react.PropsWithChildren
import react.create
import react.router.RouterProvider
import react.router.createMemoryRouter

val TestRouter by nfc<PropsWithChildren> { props ->
    RouterProvider {
        router = createMemoryRouter(
            arrayOf(
                jso {
                    path = "*"
                    element = Fragment.create { +props.children }
                },
            ),
        )
    }
}
