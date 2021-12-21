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
import kotlinx.css.*
import react.RBuilder
import react.dom.div
import react.dom.i
import react.dom.span
import react.fc
import react.router.dom.Link
import styled.css
import styled.styledDiv
import styled.styledSpan

private val styles = useStyles("About")

val AboutPage = fc<PageProps> {
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

private fun RBuilder.backButton() = Link {
    attrs.to = "/tribes"
    couplingButton(large, blue) {
        i(classes = "fa fa-step-backward") {}
        span { +"Back to Coupling!" }
    }
}

private fun RBuilder.playerHeader() = div {
    val tribeId = TribeId("developers")
    val rob by playerImage()
    val autumn by playerImage()

    listOf(
        "left" to Player("1", name = "RoB", imageURL = rob),
        "right" to Player("2", name = "Autumn", imageURL = autumn)
    ).forEach { (side, player) ->
        playerCard(PlayerCardProps(tribeId, player, className = playerCardStyles(side)))
    }
}

private fun playerCardStyles(sideClassName: String) = listOf(
    styles["player"],
    styles[sideClassName]
).joinToString(" ")
