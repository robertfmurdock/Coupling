package com.zegreatrob.coupling.client

import com.zegreatrob.minreact.DataProps
import react.Props
import react.ReactNode
import react.create
import react.key

fun <P : DataProps<P>> create(dataProps: DataProps<P>, key: String? = null): ReactNode = dataProps.component.create {
    +dataProps.unsafeCast<Props>()
    this.key = key
}

fun <P : DataProps<P>> DataProps<P>.create() = create(this)
