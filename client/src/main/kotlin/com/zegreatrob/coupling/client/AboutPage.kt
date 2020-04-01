package com.zegreatrob.coupling.client

import com.zegreatrob.coupling.client.external.react.*
import com.zegreatrob.coupling.client.external.reactmarkdown.markdown
import com.zegreatrob.coupling.client.player.PlayerCardProps
import com.zegreatrob.coupling.client.player.playerCard
import com.zegreatrob.coupling.client.routing.PageProps
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.tribe.TribeId
import kotlinx.css.*
import kotlinx.html.DIV
import react.RBuilder
import react.dom.*
import react.router.dom.navLink
import styled.css
import styled.styledDiv
import styled.styledSpan

object AboutPage : FRComponent<PageProps>(provider()) {

    val styles = useStyles("About")

    override fun render(props: PageProps) = reactElement {
        div(classes = styles.className) {
            div(classes = styles["content"]) {
                backButtonSection()
                markdown(loadMarkdownString("About"))
                playerHeader()
            }
        }
    }

    private fun RDOMBuilder<DIV>.backButtonSection() = styledDiv {
        css { position = Position.relative }
        styledSpan {
            css { float = Float.left; position = Position.absolute; right = (-15).px; top = 20.px }
            backButton()
        }
    }

    private fun RBuilder.backButton() = navLink(to = "/tribes") {
        button(classes = "large blue button") {
            i(classes = "fa fa-step-backward") {}
            span { +"Back to Coupling!" }
        }
    }

    private fun RDOMBuilder<DIV>.playerHeader() = div {
        val tribeId = TribeId("developers")
        listOf(
            "left" to Player("1", name = "RoB", imageURL = "/images/icons/players/robcard.small.png"),
            "right" to Player("2", name = "Autumn", imageURL = "/images/icons/players/autumncard.small.png")
        ).forEach { (side, player) ->
            playerCard(
                PlayerCardProps(tribeId, player, className = listOf(styles["player"], styles[side]).joinToString(" "))
            )
        }
    }
}