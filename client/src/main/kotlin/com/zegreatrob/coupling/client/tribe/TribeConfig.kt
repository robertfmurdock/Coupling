package com.zegreatrob.coupling.client.tribe

import com.benasher44.uuid.uuid4
import com.zegreatrob.coupling.client.DispatchFunc
import com.zegreatrob.coupling.client.Paths
import com.zegreatrob.coupling.client.external.react.useForm
import com.zegreatrob.coupling.json.*
import com.zegreatrob.coupling.model.tribe.Party
import com.zegreatrob.coupling.model.tribe.PartyId
import com.zegreatrob.coupling.repository.tribe.TribeRepository
import com.zegreatrob.minreact.DataPropsBind
import com.zegreatrob.minreact.child
import com.zegreatrob.minreact.tmFC
import react.router.Navigate
import react.useState
import kotlin.js.Json

data class TribeConfig(val tribe: Party, val dispatchFunc: DispatchFunc<out TribeConfigDispatcher>) :
    DataPropsBind<TribeConfig>(tribeConfig)

interface TribeConfigDispatcher : SaveTribeCommandDispatcher, DeleteTribeCommandDispatcher {
    override val tribeRepository: TribeRepository
}

val tribeConfig = tmFC { (tribe, commandFunc): TribeConfig ->
    val isNew = tribe.id.value == ""
    val (values, onChange) = useForm(tribe.withDefaultPartyId().toSerializable().toJsonDynamic().unsafeCast<Json>())
    val updatedTribe = values.correctTypes().fromJsonDynamic<JsonTribe>().toModel()
    val (redirectUrl, setRedirectUrl) = useState<String?>(null)
    val redirectToTribeList = { setRedirectUrl(Paths.tribeList()) }
    val onSave = commandFunc({ SaveTribeCommand(updatedTribe) }, { redirectToTribeList() })
    val onDelete = if (isNew) null else commandFunc({ DeleteTribeCommand(tribe.id) }, { redirectToTribeList() })

    if (redirectUrl != null)
        Navigate { to = redirectUrl }
    else {
        child(TribeConfigContent(updatedTribe, isNew, onChange, onSave, onDelete))
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
