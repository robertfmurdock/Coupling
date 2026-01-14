package com.zegreatrob.coupling.client.components

import com.zegreatrob.minreact.ReactFunc
import com.zegreatrob.minreact.nfc
import react.PropsWithChildren
import tanstack.react.router.Link
import tanstack.router.core.RoutePath

external interface CouplingDropDownLinkProps : PropsWithChildren {
    var to: String
}

@ReactFunc
val CouplingDropDownLink by nfc<CouplingDropDownLinkProps> { props ->
    Link {
        to = RoutePath(props.to)
        tabIndex = -1
        draggable = false
        CouplingDropDownElement { +props.children }
    }
}
