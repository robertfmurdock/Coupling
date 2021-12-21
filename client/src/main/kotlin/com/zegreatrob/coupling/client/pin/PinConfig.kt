package com.zegreatrob.coupling.client.pin

import com.zegreatrob.coupling.client.DispatchFunc
import com.zegreatrob.coupling.client.child
import com.zegreatrob.coupling.client.configFrame
import com.zegreatrob.coupling.client.external.react.get
import com.zegreatrob.coupling.client.external.react.useStyles
import com.zegreatrob.coupling.client.reactFunction
import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.coupling.model.tribe.Tribe
import com.zegreatrob.minreact.DataProps
import com.zegreatrob.minreact.TMFC
import react.RBuilder
import react.dom.div

data class PinConfig(
    val tribe: Tribe,
    val pin: Pin,
    val pinList: List<Pin>,
    val reload: () -> Unit,
    val dispatchFunc: DispatchFunc<out PinCommandDispatcher>
) : DataProps<PinConfig> {
    override val component: TMFC<PinConfig> get() = pinConfig
}

private val styles = useStyles("pin/PinConfig")

val pinConfig = reactFunction { (tribe, pin, pinList, reload, commandFunc): PinConfig ->
    configFrame(styles.className) {
        pinConfigEditor(tribe, pin, commandFunc, reload)
        pinBag(tribe, pinList, styles["pinBag"])
    }
}

private fun RBuilder.pinBag(tribe: Tribe, pinList: List<Pin>, className: String) = div(classes = className) {
    pinList.map { pin -> child(PinCard(tribe.id, pin), key = pin.id) }
}
