package com.zegreatrob.coupling.client

import com.zegreatrob.minreact.child
import kotlinx.css.Color
import react.FC
import react.PropsWithChildren
import react.PropsWithClassName

external interface ConfigFrameProps : PropsWithClassName, PropsWithChildren {
    var borderColor: Color?
    var backgroundColor: Color?
}

val ConfigFrame = FC<ConfigFrameProps> { props ->
    child(
        PageFrame(
            className = props.className,
            borderColor = props.borderColor ?: Color.black,
            backgroundColor = props.backgroundColor ?: Color("hsla(45, 80%, 96%, 1)")
        )
    ) {
        +props.children
    }
}
