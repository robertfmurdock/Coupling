package com.zegreatrob.coupling.client.pin

import com.zegreatrob.coupling.client.DispatchFunc
import com.zegreatrob.coupling.client.external.react.useForm
import com.zegreatrob.coupling.client.external.w3c.requireConfirmation
import com.zegreatrob.coupling.components.Paths.pinListPath
import com.zegreatrob.coupling.json.JsonPinData
import com.zegreatrob.coupling.json.fromJsonDynamic
import com.zegreatrob.coupling.json.toJsonDynamic
import com.zegreatrob.coupling.json.toModel
import com.zegreatrob.coupling.json.toSerializable
import com.zegreatrob.coupling.model.party.Party
import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.coupling.repository.pin.PinRepository
import com.zegreatrob.minreact.DataPropsBind
import com.zegreatrob.minreact.TMFC
import com.zegreatrob.minreact.add
import com.zegreatrob.minreact.tmFC
import react.router.Navigate
import react.useState
import kotlin.js.Json

data class PinConfig<D>(
    val party: Party,
    val pin: Pin,
    val pinList: List<Pin>,
    val reload: () -> Unit,
    val dispatchFunc: DispatchFunc<out D>
) : DataPropsBind<PinConfig<D>>(pinConfig.unsafeCast<TMFC<PinConfig<D>>>())
    where D : SavePinCommandDispatcher, D : DeletePinCommandDispatcher

private interface DD : SavePinCommandDispatcher, DeletePinCommandDispatcher {
    override val pinRepository: PinRepository
}

private val pinConfig = tmFC<PinConfig<DD>> { (party, pin, pinList, reload, dispatchFunc) ->
    val (values, onChange) = useForm(pin.toSerializable().toJsonDynamic().unsafeCast<Json>())

    val updatedPin = values.fromJsonDynamic<JsonPinData>().toModel()
    val (redirectUrl, setRedirectUrl) = useState<String?>(null)
    val onSubmit = dispatchFunc({ SavePinCommand(party.id, updatedPin) }) { reload() }
    val onRemove = pin.id?.let { pinId ->
        dispatchFunc({ DeletePinCommand(party.id, pinId) }) { setRedirectUrl(party.id.pinListPath()) }
            .requireConfirmation("Are you sure you want to delete this pin?")
    }

    if (redirectUrl != null)
        Navigate { to = redirectUrl }
    else
        add(PinConfigContent(party, updatedPin, pinList, onChange, onSubmit, onRemove))
}
