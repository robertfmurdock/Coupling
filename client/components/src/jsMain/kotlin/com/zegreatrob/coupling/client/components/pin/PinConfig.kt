package com.zegreatrob.coupling.client.components.pin

import com.zegreatrob.coupling.action.pin.DeletePinCommand
import com.zegreatrob.coupling.action.pin.SavePinCommand
import com.zegreatrob.coupling.action.pin.fire
import com.zegreatrob.coupling.client.components.DispatchFunc
import com.zegreatrob.coupling.client.components.Paths.pinListPath
import com.zegreatrob.coupling.client.components.external.w3c.requireConfirmation
import com.zegreatrob.coupling.client.components.useForm
import com.zegreatrob.coupling.json.JsonPinData
import com.zegreatrob.coupling.json.fromJsonDynamic
import com.zegreatrob.coupling.json.toJsonDynamic
import com.zegreatrob.coupling.json.toModel
import com.zegreatrob.coupling.json.toSerializable
import com.zegreatrob.coupling.model.Boost
import com.zegreatrob.coupling.model.party.PartyDetails
import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.minreact.ReactFunc
import com.zegreatrob.minreact.nfc
import js.objects.jso
import react.Props
import react.router.Navigate
import react.router.dom.usePrompt
import react.useState
import kotlin.js.Json

external interface PinConfigProps<D> : Props where D : DeletePinCommand.Dispatcher, D : SavePinCommand.Dispatcher {
    var party: PartyDetails
    var boost: Boost?
    var pin: Pin
    var pinList: List<Pin>
    var reload: () -> Unit
    var dispatchFunc: DispatchFunc<D>
}

@ReactFunc
val PinConfig by nfc<PinConfigProps<*>> { props ->
    val (party, boost, pin, pinList, reload, dispatchFunc) = props
    val (values, onChange) = useForm(pin.toSerializable().toJsonDynamic().unsafeCast<Json>())

    val updatedPin = values.fromJsonDynamic<JsonPinData>().toModel()
    val (redirectUrl, setRedirectUrl) = useState<String?>(null)
    val onSubmit = dispatchFunc {
        fire(SavePinCommand(party.id, updatedPin))
        reload()
    }
    val onRemove = if (pin.id.isBlank()) {
        null
    } else {
        dispatchFunc {
            fire(DeletePinCommand(party.id, pin.id))
            setRedirectUrl(party.id.pinListPath())
        }.requireConfirmation("Are you sure you want to delete this pin?")
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
        PinConfigContent(party, boost, updatedPin, pinList, onChange, onSubmit, onRemove)
    }
}
