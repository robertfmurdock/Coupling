package com.zegreatrob.coupling.client.party

import com.zegreatrob.coupling.client.CardHeader
import com.zegreatrob.coupling.client.external.react.get
import com.zegreatrob.coupling.client.external.react.useStyles
import com.zegreatrob.coupling.model.party.Party
import com.zegreatrob.minreact.DataPropsBind
import com.zegreatrob.minreact.tmFC

private val styles = useStyles("party/TribeCard")

data class PartyCardHeader(val tribe: Party, val size: Int) : DataPropsBind<PartyCardHeader>(partyCardHeader)

val partyCardHeader = tmFC<PartyCardHeader> { (party, size) ->
    CardHeader {
        this.size = size
        this.className = styles["header"]
        this.headerContent = party.name ?: ""
    }
}
