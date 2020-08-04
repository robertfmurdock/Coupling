package com.zegreatrob.coupling.client

import com.zegreatrob.coupling.client.dom.blue
import com.zegreatrob.coupling.client.dom.couplingButton
import com.zegreatrob.coupling.client.dom.large
import com.zegreatrob.coupling.client.external.react.get
import com.zegreatrob.coupling.client.external.react.loadMarkdownString
import com.zegreatrob.coupling.client.external.react.useStyles
import com.zegreatrob.coupling.client.external.reactmarkdown.markdown
import com.zegreatrob.coupling.client.player.PlayerCardProps
import com.zegreatrob.coupling.client.player.playerCard
import com.zegreatrob.coupling.client.routing.PageProps
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.minreact.reactFunction
import kotlinx.css.*
import react.RBuilder
import react.dom.div
import react.dom.i
import react.dom.span
import react.router.dom.routeLink
import styled.css
import styled.styledDiv
import styled.styledSpan

private val styles = useStyles("About")

val AboutPage = reactFunction<PageProps> {
    div(classes = styles.className) {
        div(classes = styles["content"]) {
            backButtonSection()
            markdown(loadMarkdownString("About"))
            playerHeader()
        }
    }
}

private fun RBuilder.backButtonSection() = styledDiv {
    css { position = Position.relative }
    styledSpan {
        css { float = Float.left; position = Position.absolute; right = (-15).px; top = 20.px }
        backButton()
    }
}

private fun RBuilder.backButton() = routeLink(to = "/tribes") {
    couplingButton(large, blue) {
        i(classes = "fa fa-step-backward") {}
        span { +"Back to Coupling!" }
    }
}

val robCardPath = kotlinext.js.require("players/robcard.small.png").default.unsafeCast<String>()
    .let { "/app/build/$it" }

val autumnCardPath = kotlinext.js.require("players/autumncard.small.png").default.unsafeCast<String>()
    .let { "/app/build/$it" }


private fun RBuilder.playerHeader() = div {
    val tribeId = TribeId("developers")
    listOf(
        "left" to Player("1", name = "RoB", imageURL = robCardPath),
        "right" to Player("2", name = "Autumn", imageURL = autumnCardPath)
    ).forEach { (side, player) ->
        playerCard(
            PlayerCardProps(tribeId, player, className = playerCardStyles(side))
        )
    }
}

private fun playerCardStyles(sideClassName: String) = listOf(
    styles["player"],
    styles[sideClassName]
).joinToString(" ")
