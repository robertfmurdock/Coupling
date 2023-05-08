package com.zegreatrob.coupling.client.components.pin

import com.zegreatrob.coupling.action.pin.DeletePinCommand
import com.zegreatrob.coupling.action.pin.SavePinCommand
import com.zegreatrob.coupling.client.components.DispatchFunc
import com.zegreatrob.coupling.client.components.Paths.pinListPath
import com.zegreatrob.coupling.client.components.external.w3c.requireConfirmation
import com.zegreatrob.coupling.client.components.useForm
import com.zegreatrob.coupling.json.JsonPinData
import com.zegreatrob.coupling.json.fromJsonDynamic
import com.zegreatrob.coupling.json.toJsonDynamic
import com.zegreatrob.coupling.json.toModel
import com.zegreatrob.coupling.json.toSerializable
import com.zegreatrob.coupling.model.party.Party
import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.minreact.DataPropsBind
import com.zegreatrob.minreact.TMFC
import com.zegreatrob.minreact.add
import com.zegreatrob.minreact.ntmFC
import js.core.jso
import react.router.Navigate
import react.router.dom.usePrompt
import react.useState
import kotlin.js.Json

data class PinConfig<D>(
    val party: Party,
    val pin: Pin,
    val pinList: List<Pin>,
    val reload: () -> Unit,
    val dispatchFunc: DispatchFunc<out D>,
) : DataPropsBind<PinConfig<D>>(pinConfig.unsafeCast<TMFC>())
    where D : SavePinCommand.Dispatcher, D : DeletePinCommand.Dispatcher

private interface DD : SavePinCommand.Dispatcher, DeletePinCommand.Dispatcher

private val pinConfig by ntmFC<PinConfig<DD>> { (party, pin, pinList, reload, dispatchFunc) ->
    val (values, onChange) = useForm(pin.toSerializable().toJsonDynamic().unsafeCast<Json>())

    val updatedPin = values.fromJsonDynamic<JsonPinData>().toModel()
    val (redirectUrl, setRedirectUrl) = useState<String?>(null)
    val onSubmit = dispatchFunc({ SavePinCommand(party.id, updatedPin) }) { reload() }
    val onRemove = pin.id?.let { pinId ->
        dispatchFunc({ DeletePinCommand(party.id, pinId) }) { setRedirectUrl(party.id.pinListPath()) }
            .requireConfirmation("Are you sure you want to delete this pin?")
    }
    usePrompt(
        jso {
            `when` = updatedPin != pin
            message = "You have unsaved data. Press OK to leave without saving."
        },
    )
    if (redirectUrl != null) {
        Navigate { to = redirectUrl }
    } else {
        add(PinConfigContent(party, updatedPin, pinList, onChange, onSubmit, onRemove))
    }
}
