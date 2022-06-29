package com.zegreatrob.coupling.client.party

import com.benasher44.uuid.uuid4
import com.zegreatrob.coupling.components.DispatchFunc
import com.zegreatrob.coupling.components.Paths
import com.zegreatrob.coupling.components.useForm
import com.zegreatrob.coupling.json.JsonParty
import com.zegreatrob.coupling.json.fromJsonDynamic
import com.zegreatrob.coupling.json.toJsonDynamic
import com.zegreatrob.coupling.json.toModel
import com.zegreatrob.coupling.json.toSerializable
import com.zegreatrob.coupling.model.party.Party
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.repository.party.PartyRepository
import com.zegreatrob.minreact.DataPropsBind
import com.zegreatrob.minreact.TMFC
import com.zegreatrob.minreact.add
import com.zegreatrob.minreact.tmFC
import react.router.Navigate
import react.useState
import kotlin.js.Json

data class PartyConfig<D>(val tribe: Party, val dispatchFunc: DispatchFunc<out D>) :
    DataPropsBind<PartyConfig<D>>(partyConfig.unsafeCast<TMFC<PartyConfig<D>>>())
    where D : SavePartyCommandDispatcher, D : DeletePartyCommandDispatcher

private interface PartyConfigDispatcher : SavePartyCommandDispatcher, DeletePartyCommandDispatcher {
    override val partyRepository: PartyRepository
}

private val partyConfig = tmFC { (party, commandFunc): PartyConfig<PartyConfigDispatcher> ->
    val isNew = party.id.value == ""
    val (values, onChange) = useForm(party.withDefaultPartyId().toSerializable().toJsonDynamic().unsafeCast<Json>())
    val updatedParty = values.correctTypes().fromJsonDynamic<JsonParty>().toModel()
    val (redirectUrl, setRedirectUrl) = useState<String?>(null)
    val redirectToPartyList = { setRedirectUrl(Paths.partyList()) }
    val onSave = commandFunc({ SavePartyCommand(updatedParty) }, { redirectToPartyList() })
    val onDelete = if (isNew) null else commandFunc({ DeletePartyCommand(party.id) }, { redirectToPartyList() })

    if (redirectUrl != null)
        Navigate { to = redirectUrl }
    else {
        add(PartyConfigContent(updatedParty, isNew, onChange, onSave, onDelete))
    }
}

private fun Json.correctTypes() = also {
    set("animationSpeed", this["animationSpeed"].toString().toDouble())
    set("pairingRule", this["pairingRule"].toString().toInt())
}

private fun Party.withDefaultPartyId() = if (id.value.isNotBlank())
    this
else
    copy(id = PartyId("${uuid4()}"))
