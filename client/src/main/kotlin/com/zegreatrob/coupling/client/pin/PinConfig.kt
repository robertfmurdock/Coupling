package com.zegreatrob.coupling.client.pin

import com.zegreatrob.coupling.client.ConfigFrame
import com.zegreatrob.coupling.client.DispatchFunc
import com.zegreatrob.coupling.client.external.react.get
import com.zegreatrob.coupling.client.external.react.useStyles
import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.coupling.model.tribe.Tribe
import com.zegreatrob.minreact.DataProps
import com.zegreatrob.minreact.TMFC
import com.zegreatrob.minreact.child
import com.zegreatrob.minreact.tmFC
import react.ChildrenBuilder
import react.dom.html.ReactHTML.div

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

val pinConfig = tmFC { (tribe, pin, pinList, reload, commandFunc): PinConfig ->
    ConfigFrame {
        className = styles.className
        child(PinConfigEditor(tribe, pin, reload, commandFunc))
        pinBag(tribe, pinList, styles["pinBag"])
    }
}

private fun ChildrenBuilder.pinBag(tribe: Tribe, pinList: List<Pin>, className: String) = div {
    this.className = className
    pinList.map { pin -> child(PinCard(tribe.id, pin), key = pin.id) }
}
