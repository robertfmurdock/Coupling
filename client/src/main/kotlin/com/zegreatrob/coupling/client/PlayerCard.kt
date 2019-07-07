package com.zegreatrob.coupling.client

import react.RBuilder
import react.ReactElement
import react.dom.div

private val styles: dynamic = js("require('../../resources/main/com/zegreatrob/coupling/client/PlayerCard.css')")


fun RBuilder.playerCard(): ReactElement {
    return div(classes = styles.player as? String) { }
}