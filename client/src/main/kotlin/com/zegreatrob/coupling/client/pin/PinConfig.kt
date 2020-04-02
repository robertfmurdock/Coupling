package com.zegreatrob.coupling.client.pin

import com.zegreatrob.coupling.client.ConfigFrame.configFrame
import com.zegreatrob.coupling.client.external.react.*
import com.zegreatrob.coupling.client.external.w3c.WindowFunctions
import com.zegreatrob.coupling.client.pairassignments.NullTraceIdProvider
import com.zegreatrob.coupling.client.pin.PinCard.pinCard
import com.zegreatrob.coupling.client.pin.PinConfigEditor.pinConfigEditor
import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.coupling.model.tribe.Tribe
import com.zegreatrob.coupling.repository.pin.PinRepository
import com.zegreatrob.coupling.sdk.RepositoryCatalog
import com.zegreatrob.coupling.sdk.SdkSingleton
import react.RBuilder
import react.RProps
import react.dom.div

object PinConfig : RComponent<PinConfigProps>(provider()), PinConfigRenderer, RepositoryCatalog by SdkSingleton

data class PinConfigProps(
    val tribe: Tribe,
    val pin: Pin,
    val pinList: List<Pin>,
    val pathSetter: (String) -> Unit,
    val reload: () -> Unit
) : RProps

external interface PinConfigStyles {
    val className: String
    val icon: String
    val saveButton: String
    val tribeBrowser: String
    val pin: String
    val deleteButton: String
    val pinBag: String
}

typealias PinConfigContext = ScopedStyledRContext<PinConfigProps, PinConfigStyles>

interface PinConfigRenderer : ScopedStyledComponentRenderer<PinConfigProps, PinConfigStyles>,
    WindowFunctions, SavePinCommandDispatcher, DeletePinCommandDispatcher, NullTraceIdProvider {

    override val pinRepository: PinRepository
    override val componentPath: String get() = "pin/PinConfig"

    override fun PinConfigContext.render() = reactElement {
        val (tribe, pin, pinList, pathSetter, reload) = props
        configFrame(styles.className) {
            pinConfigEditor(tribe, pin, pathSetter, reload)
            pinBag(tribe, pinList, styles.pinBag)
        }
    }

    private inline fun RBuilder.pinBag(tribe: Tribe, pinList: List<Pin>, className: String) = div(classes = className) {
        pinList.map { pin -> pinCard(tribe.id, pin, key = pin._id) }
    }

}

