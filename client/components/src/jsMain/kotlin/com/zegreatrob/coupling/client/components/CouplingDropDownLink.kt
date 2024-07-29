package com.zegreatrob.coupling.client.components

import com.zegreatrob.minreact.ReactFunc
import com.zegreatrob.minreact.nfc
import react.PropsWithChildren
import react.router.dom.Link

external interface CouplingDropDownLinkProps : PropsWithChildren {
    var to: String
}

@ReactFunc
val CouplingDropDownLink by nfc<CouplingDropDownLinkProps> { props ->
    Link {
        to = props.to
        tabIndex = -1
        draggable = false
        CouplingDropDownElement { +props.children }
    }
}
