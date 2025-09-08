package com.zegreatrob.coupling.client.components

import com.zegreatrob.minreact.nfc
import js.objects.unsafeJso
import react.Fragment
import react.PropsWithChildren
import react.create
import react.router.RouterProvider
import react.router.createMemoryRouter

val TestRouter by nfc<PropsWithChildren> { props ->
    RouterProvider {
        router = createMemoryRouter(
            arrayOf(
                unsafeJso {
                    path = "*"
                    element = Fragment.create { +props.children }
                },
            ),
        )
    }
}
