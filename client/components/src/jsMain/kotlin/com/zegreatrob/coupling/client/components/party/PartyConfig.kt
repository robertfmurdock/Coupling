package com.zegreatrob.coupling.client.components.party

import com.benasher44.uuid.uuid4
import com.zegreatrob.coupling.action.party.DeletePartyCommand
import com.zegreatrob.coupling.action.party.SavePartyCommand
import com.zegreatrob.coupling.action.party.fire
import com.zegreatrob.coupling.client.components.DispatchFunc
import com.zegreatrob.coupling.client.components.Paths
import com.zegreatrob.coupling.client.components.useForm
import com.zegreatrob.coupling.json.JsonPartyDetails
import com.zegreatrob.coupling.json.fromJsonDynamic
import com.zegreatrob.coupling.json.toJsonDynamic
import com.zegreatrob.coupling.json.toModel
import com.zegreatrob.coupling.json.toSerializable
import com.zegreatrob.coupling.model.party.PartyDetails
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.minreact.ReactFunc
import com.zegreatrob.minreact.nfc
import react.Props
import react.router.Navigate
import react.useState
import kotlin.js.Json

external interface PartyConfigProps<D> : Props
    where D : SavePartyCommand.Dispatcher, D : DeletePartyCommand.Dispatcher {
    var party: PartyDetails
    var dispatchFunc: DispatchFunc<out D>
}

@ReactFunc
val PartyConfig by nfc<PartyConfigProps<*>> { (party, commandFunc) ->
    val isNew = party.id.value == ""
    val (values, onChange) = useForm(party.withDefaultPartyId().toSerializable().toJsonDynamic().unsafeCast<Json>())
    val updatedParty = values.correctTypes().fromJsonDynamic<JsonPartyDetails>().toModel()
    val (redirectUrl, setRedirectUrl) = useState<String?>(null)
    val redirectToPartyList = { setRedirectUrl(Paths.partyList()) }
    val onSave = commandFunc {
        fire(SavePartyCommand(updatedParty))
        redirectToPartyList()
    }
    val onDelete = if (isNew) {
        null
    } else {
        commandFunc {
            fire(DeletePartyCommand(party.id))
            redirectToPartyList()
        }
    }

    if (redirectUrl != null) {
        Navigate { to = redirectUrl }
    } else {
        PartyConfigContent(updatedParty, isNew, onChange, onSave, onDelete)
    }
}

private fun Json.correctTypes() = also {
    set("animationSpeed", this["animationSpeed"].toString().toDouble())
    set("pairingRule", this["pairingRule"].toString().toInt())
}

private fun PartyDetails.withDefaultPartyId() = if (id.value.isNotBlank()) {
    this
} else {
    copy(id = PartyId("${uuid4()}"))
}
