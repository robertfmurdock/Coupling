package com.zegreatrob.coupling.client.incubating

import com.zegreatrob.coupling.client.components.PageFrame
import com.zegreatrob.coupling.client.routing.CouplingQuery
import com.zegreatrob.coupling.client.routing.PageProps
import com.zegreatrob.coupling.sdk.gql.graphQuery
import com.zegreatrob.minreact.ReactFunc
import com.zegreatrob.minreact.nfc
import react.Props
import react.dom.html.ReactHTML.div
import web.cssom.Color

val IncubatingPage by nfc<PageProps> { props ->
    CouplingQuery(
        commander = props.commander,
        query = graphQuery { addToSlackUrl() },
        toNode = { _, _, result -> result.addToSlackUrl?.let(IncubatingContent::create) },
    )
}

external interface IncubatingContentProps : Props {
    var addToSlackUrl: String
}

@ReactFunc
val IncubatingContent by nfc<IncubatingContentProps> { props ->
    PageFrame(borderColor = Color("#e8e8e8"), backgroundColor = Color("#dcd9d9")) {
        +"Incubating Features - Best not to touch"
        div {
            AddToSlackButton { url = props.addToSlackUrl }
        }
    }
}
