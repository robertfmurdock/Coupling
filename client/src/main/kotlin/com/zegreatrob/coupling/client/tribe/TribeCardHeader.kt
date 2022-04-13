package com.zegreatrob.coupling.client.tribe

import com.zegreatrob.coupling.client.CardHeader
import com.zegreatrob.coupling.client.external.react.get
import com.zegreatrob.coupling.client.external.react.useStyles
import com.zegreatrob.coupling.model.party.Party
import com.zegreatrob.minreact.DataPropsBind
import com.zegreatrob.minreact.tmFC

private val styles = useStyles("tribe/TribeCard")

data class TribeCardHeader(val tribe: Party, val size: Int) : DataPropsBind<TribeCardHeader>(tribeCardHeader)

val tribeCardHeader = tmFC<TribeCardHeader> { (tribe, size) ->
    CardHeader {
        this.size = size
        this.className = styles["header"]
        this.headerContent = tribe.name ?: ""
    }
}
