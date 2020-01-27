package com.zegreatrob.coupling.client.pin

import com.zegreatrob.coupling.client.external.react.FRComponent
import com.zegreatrob.coupling.client.external.react.provider
import react.RProps
import react.ReactElement

object DraggablePin : FRComponent<DraggablePinProps>(provider()) {

    override fun render(props: DraggablePinProps): ReactElement {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}

data class DraggablePinProps(val lol: String) : RProps