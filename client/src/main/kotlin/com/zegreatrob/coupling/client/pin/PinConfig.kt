package com.zegreatrob.coupling.client.pin

import com.zegreatrob.coupling.client.CommandDispatcher
import com.zegreatrob.coupling.client.ConfigFrame.configFrame
import com.zegreatrob.coupling.client.external.react.*
import com.zegreatrob.coupling.client.pin.PinCard.pinCard
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

object PinConfig : FRComponent<PinConfigProps>(provider()) {

    val styles = useStyles("pin/PinConfig")

    override fun render(props: PinConfigProps) = reactElement {
        val (tribe, pin, pinList, pathSetter, reload) = props
        configFrame(styles.className) {
            pinConfigEditor(tribe, pin, CommandDispatcher, pathSetter, reload)
            pinBag(tribe, pinList, styles["pinBag"])
        }
    }

    private fun RBuilder.pinBag(tribe: Tribe, pinList: List<Pin>, className: String) = div(classes = className) {
        pinList.map { pin -> pinCard(tribe.id, pin, key = pin._id) }
    }

}
