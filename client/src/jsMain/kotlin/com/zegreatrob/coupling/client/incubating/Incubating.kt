package com.zegreatrob.coupling.client.incubating

import com.zegreatrob.coupling.client.components.PageFrame
import com.zegreatrob.coupling.client.routing.CouplingQuery
import com.zegreatrob.coupling.client.routing.PageProps
import com.zegreatrob.coupling.sdk.gql.graphQuery
import com.zegreatrob.minreact.DataPropsBind
import com.zegreatrob.minreact.add
import com.zegreatrob.minreact.nfc
import com.zegreatrob.minreact.ntmFC
import react.dom.html.ReactHTML.div
import web.cssom.Color

val IncubatingPage by nfc<PageProps> { props ->
    add(
        CouplingQuery(
            commander = props.commander,
            query = graphQuery { addToSlackUrl() },
            toDataprops = { _, _, result ->
                IncubatingContent(
                    addToSlackUrl = result.addToSlackUrl ?: return@CouplingQuery null,
                )
            },
        ),
    )
}

data class IncubatingContent(val addToSlackUrl: String) : DataPropsBind<IncubatingContent>(incubatingContent)

val incubatingContent by ntmFC<IncubatingContent> { props ->
    PageFrame(borderColor = Color("#e8e8e8"), backgroundColor = Color("#dcd9d9")) {
        +"Incubating Features - Best not to touch"
        div {
            AddToSlackButton { url = props.addToSlackUrl }
        }
    }
}
