package com.zegreatrob.coupling.client.tribe

import com.zegreatrob.coupling.client.ConfigHeader
import com.zegreatrob.coupling.client.external.react.useStyles
import com.zegreatrob.coupling.model.party.Party
import com.zegreatrob.minreact.DataPropsBind
import com.zegreatrob.minreact.tmFC
import react.dom.html.ReactHTML.div

data class TribeBrowser(val tribe: Party) : DataPropsBind<TribeBrowser>(tribeBrowser)

private val styles = useStyles("tribe/TribeBrowser")

val tribeBrowser = tmFC<TribeBrowser> { (tribe) ->
    div {
        className = styles.className
        ConfigHeader {
            this.tribe = tribe
            +(tribe.name ?: "")
        }
    }
}
