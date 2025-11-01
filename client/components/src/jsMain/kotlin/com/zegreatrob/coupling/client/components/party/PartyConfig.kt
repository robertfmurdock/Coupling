package com.zegreatrob.coupling.client.components.party

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
import com.zegreatrob.coupling.model.Boost
import com.zegreatrob.coupling.model.party.PartyDetails
import com.zegreatrob.minreact.ReactFunc
import com.zegreatrob.minreact.nfc
import react.Props
import react.router.Navigate
import react.useState
import kotlin.js.Json

external interface PartyConfigProps<D> : Props
    where D : SavePartyCommand.Dispatcher, D : DeletePartyCommand.Dispatcher {
    var party: PartyDetails
    var boost: Boost?
    var dispatchFunc: DispatchFunc<D>
    var isNew: Boolean
}

@ReactFunc
val PartyConfig by nfc<PartyConfigProps<*>> { (party, boost, commandFunc, isNew) ->
    val (values, onChange) = useForm(party.toSerializable().toJsonDynamic().unsafeCast<Json>())
    val updatedParty = values.correctTypes().fromJsonDynamic<JsonPartyDetails>().toModel()
    val (redirectUrl, setRedirectUrl) = useState<String?>(null)
    val redirectToPartyList = { setRedirectUrl(Paths.partyList()) }
    val onSave = commandFunc {
        fire(SavePartyCommand(updatedParty))
        redirectToPartyList()
    }
    val onDelete = commandFunc {
        fire(DeletePartyCommand(party.id))
        redirectToPartyList()
    }.takeUnless { isNew }

    if (redirectUrl != null) {
        Navigate { to = redirectUrl }
    } else {
        PartyConfigContent(
            party = updatedParty,
            boost = boost,
            isNew = isNew,
            onChange = onChange,
            onSave = onSave,
            onDelete = onDelete,
        )
    }
}

private fun Json.correctTypes() = also {
    set("animationSpeed", this["animationSpeed"].toString().toDouble())
    set("pairingRule", this["pairingRule"].toString().toInt())
}
