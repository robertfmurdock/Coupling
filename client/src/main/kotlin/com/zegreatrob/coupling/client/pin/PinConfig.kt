package com.zegreatrob.coupling.client.pin

import com.zegreatrob.coupling.client.CommandDispatcher
import com.zegreatrob.coupling.client.ConfigFrame.configFrame
import com.zegreatrob.coupling.client.external.react.get
import com.zegreatrob.coupling.client.external.react.reactFunction
import com.zegreatrob.coupling.client.external.react.useStyles
import com.zegreatrob.coupling.client.pin.PinConfigEditor.Companion.pinConfigEditor
import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.coupling.model.tribe.Tribe
import react.RBuilder
import react.RProps
import react.dom.div

data class PinConfigProps(
    val tribe: Tribe,
    val pin: Pin,
    val pinList: List<Pin>,
    val pathSetter: (String) -> Unit,
    val reload: () -> Unit
) : RProps

private val styles = useStyles("pin/PinConfig")

val PinConfig = reactFunction<PinConfigProps> { (tribe, pin, pinList, pathSetter, reload) ->
    configFrame(styles.className) {
        pinConfigEditor(tribe, pin, CommandDispatcher, pathSetter, reload)
        pinBag(tribe, pinList, styles["pinBag"])
    }
}

private fun RBuilder.pinBag(tribe: Tribe, pinList: List<Pin>, className: String) = div(classes = className) {
    pinList.map { pin -> pinCard(tribe.id, pin, key = pin._id) }
}
