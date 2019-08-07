package com.zegreatrob.coupling.client.external.react

import react.*

interface ReactComponentRenderer {
    fun <P : RProps> RBuilder.element(clazz: RClass<P>, props: P, key: String? = null, handler: RHandler<P> = {}) {
        key?.let { props.key = it }
        child(
                type = clazz,
                props = props,
                handler = handler
        )
    }
}
