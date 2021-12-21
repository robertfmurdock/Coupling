package com.zegreatrob.coupling.client.external.react

import com.zegreatrob.minreact.DataProps
import com.zegreatrob.minreact.DataPropsBridge
import com.zegreatrob.minreact.TMFC
import org.w3c.dom.Node
import react.MutableRefObject
import react.RBuilder
import react.key
import react.ref

class BuilderAdapter<P : DataProps<P>>(val builder: RBuilder, val component: TMFC<P>) {
    operator fun invoke(
        props: P,
        key: String? = null,
        ref: MutableRefObject<Node>? = null,
        handler: RBuilder.(P) -> Unit = {}
    ) = builder.child(
        type = component,
        props = props.unsafeCast<DataPropsBridge<P>>(),
        handler = {
            attrs {
                key?.let { this.key = it }
                ref?.let { this.ref = ref }
            }
            handler(props)
        }
    )
}

fun <P : DataProps<P>> RBuilder.childCurry(component: TMFC<P>) = BuilderAdapter(this, component)
