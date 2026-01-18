package com.zegreatrob.coupling.client.components.pin

import com.zegreatrob.coupling.action.pin.DeletePinCommand
import com.zegreatrob.coupling.action.pin.SavePinCommand
import com.zegreatrob.coupling.action.pin.fire
import com.zegreatrob.coupling.client.components.DispatchFunc
import com.zegreatrob.coupling.client.components.Paths.pinListPath
import com.zegreatrob.coupling.client.components.external.tanstack.reactrouter.UseBlockerOptions
import com.zegreatrob.coupling.client.components.external.tanstack.reactrouter.useBlocker
import com.zegreatrob.coupling.client.components.external.w3c.requireConfirmation
import com.zegreatrob.coupling.client.components.useForm
import com.zegreatrob.coupling.json.GqlPinSnapshot
import com.zegreatrob.coupling.json.fromJsonDynamic
import com.zegreatrob.coupling.json.toJsonDynamic
import com.zegreatrob.coupling.json.toModel
import com.zegreatrob.coupling.json.toSerializable
import com.zegreatrob.coupling.model.Boost
import com.zegreatrob.coupling.model.party.PartyDetails
import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.minreact.ReactFunc
import com.zegreatrob.minreact.nfc
import js.objects.unsafeJso
import react.Props
import react.useEffect
import react.useState
import tanstack.react.router.useNavigate
import tanstack.router.core.RoutePath
import web.prompts.confirm
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

    val updatedPin = values.fromJsonDynamic<GqlPinSnapshot>().toModel()
    val (redirectUrl, setRedirectUrl) = useState<String?>(null)
    useBlocker(
        UseBlockerOptions {
            if (updatedPin == pin) {
                false
            } else {
                !confirm("You have unsaved data. Press OK to leave without saving.")
            }
        },
    )

    val navigate = useNavigate()
    useEffect(redirectUrl) {
        if (redirectUrl != null) {
            navigate(unsafeJso { to = RoutePath(redirectUrl) })
        }
    }

    val onSubmit = dispatchFunc {
        fire(SavePinCommand(party.id, updatedPin))
        reload()
    }
    val onRemove = if (!pinList.contains(pin)) {
        null
    } else {
        dispatchFunc {
            fire(DeletePinCommand(party.id, pin.id))
            setRedirectUrl(party.id.pinListPath())
        }.requireConfirmation("Are you sure you want to delete this pin?")
    }

    if (redirectUrl == null) {
        PinConfigContent(party, boost, updatedPin, pinList, onChange, onSubmit, onRemove)
    }
}
