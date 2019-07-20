package com.zegreatrob.coupling.client

import react.RProps

interface ComponentBuilder<P : RProps> {
    fun build(): ReactFunctionComponent<P>
}