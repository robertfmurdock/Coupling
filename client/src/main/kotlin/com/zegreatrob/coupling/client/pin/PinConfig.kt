package com.zegreatrob.coupling.client.pin

import com.zegreatrob.coupling.client.DispatchFunc
import com.zegreatrob.coupling.client.Paths.pinListPath
import com.zegreatrob.coupling.client.external.react.useForm
import com.zegreatrob.coupling.client.external.w3c.requireConfirmation
import com.zegreatrob.coupling.json.*
import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.coupling.model.party.Party
import com.zegreatrob.minreact.DataPropsBind
import com.zegreatrob.minreact.child
import com.zegreatrob.minreact.tmFC
import react.router.Navigate
import react.useState
import kotlin.js.Json

data class PinConfig(
    val tribe: Party,
    val pin: Pin,
    val pinList: List<Pin>,
    val reload: () -> Unit,
    val dispatchFunc: DispatchFunc<out PinCommandDispatcher>
) : DataPropsBind<PinConfig>(pinConfig)

val pinConfig = tmFC { (tribe, pin, pinList, reload, dispatchFunc): PinConfig ->
    val (values, onChange) = useForm(pin.toSerializable().toJsonDynamic().unsafeCast<Json>())

    val updatedPin = values.fromJsonDynamic<JsonPinData>().toModel()
    val (redirectUrl, setRedirectUrl) = useState<String?>(null)
    val onSubmit = dispatchFunc({ SavePinCommand(tribe.id, updatedPin) }) { reload() }
    val onRemove = pin.id?.let { pinId ->
        dispatchFunc({ DeletePinCommand(tribe.id, pinId) }) { setRedirectUrl(tribe.id.pinListPath()) }
            .requireConfirmation("Are you sure you want to delete this pin?")
    }

    if (redirectUrl != null)
        Navigate { to = redirectUrl }
    else
        child(PinConfigContent(tribe, updatedPin, pinList, onChange, onSubmit, onRemove))
}
