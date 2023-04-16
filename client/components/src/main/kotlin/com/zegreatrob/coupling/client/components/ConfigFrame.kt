package com.zegreatrob.coupling.client.components

import com.zegreatrob.minreact.add
import com.zegreatrob.minreact.nfc
import csstype.Color
import csstype.NamedColor
import react.PropsWithChildren
import react.PropsWithClassName

external interface ConfigFrameProps : PropsWithClassName, PropsWithChildren {
    var borderColor: Color?
    var backgroundColor: Color?
}

val ConfigFrame by nfc<ConfigFrameProps> { props ->
    add(
        PageFrame(
            className = props.className,
            borderColor = props.borderColor ?: NamedColor.black,
            backgroundColor = props.backgroundColor ?: Color("hsla(45, 80%, 96%, 1)"),
        ),
    ) {
        +props.children
    }
}
