package com.zegreatrob.coupling.client.pin

import com.zegreatrob.coupling.client.DispatchFunc
import com.zegreatrob.coupling.client.configFrame
import com.zegreatrob.coupling.client.external.react.get
import com.zegreatrob.coupling.client.external.react.useStyles
import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.coupling.model.tribe.Tribe
import com.zegreatrob.minreact.reactFunction
import react.RBuilder
import react.RProps
import react.dom.div

data class PinConfigProps(
    val tribe: Tribe,
    val pin: Pin,
    val pinList: List<Pin>,
    val pathSetter: (String) -> Unit,
    val reload: () -> Unit,
    val dispatchFunc: DispatchFunc<out PinCommandDispatcher>
) : RProps

private val styles = useStyles("pin/PinConfig")

val PinConfig = reactFunction { (tribe, pin, pinList, pathSetter, reload, commandFunc): PinConfigProps ->
    configFrame(styles.className) {
        pinConfigEditor(tribe, pin, commandFunc, reload)
        pinBag(tribe, pinList, styles["pinBag"])
    }
}

private fun RBuilder.pinBag(tribe: Tribe, pinList: List<Pin>, className: String) = div(classes = className) {
    pinList.map { pin -> pinCard(tribe.id, pin, key = pin.id) }
}
