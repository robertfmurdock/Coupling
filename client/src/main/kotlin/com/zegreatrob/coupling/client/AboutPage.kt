package com.zegreatrob.coupling.client

import com.zegreatrob.coupling.client.external.react.useStyles
import com.zegreatrob.coupling.client.routing.PageProps
import com.zegreatrob.coupling.components.CouplingButton
import com.zegreatrob.coupling.components.PageFrame
import com.zegreatrob.coupling.components.PlayerCard
import com.zegreatrob.coupling.components.blue
import com.zegreatrob.coupling.components.external.reactmarkdown.Markdown
import com.zegreatrob.coupling.components.large
import com.zegreatrob.coupling.components.loadMarkdownString
import com.zegreatrob.coupling.components.welcome.playerImage
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.minreact.add
import csstype.ClassName
import csstype.Color
import csstype.Position
import csstype.deg
import csstype.em
import csstype.px
import emotion.react.css
import react.FC
import react.Props
import react.PropsWithChildren
import react.dom.html.ReactHTML
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.i
import react.dom.html.ReactHTML.span
import react.router.dom.Link

private val styles = useStyles("About")

val AboutPage = FC<PageProps> {
    aboutPageContent {
        Markdown { +loadMarkdownString("About") }
    }
}

val aboutPageContent = FC<PropsWithChildren> { props ->
    div {
        className = styles.className
        add(PageFrame(borderColor = Color("rgb(94, 84, 102)"), backgroundColor = Color("floralwhite"))) {
            div {
                css { width = 40.em }
                backButtonSection()
                +props.children
                playerHeader()
            }
        }
    }
}

private val backButtonSection = FC<Props> {
    div {
        css { position = Position.relative }
        ReactHTML.span {
            css { float = csstype.Float.left; position = Position.absolute; right = (-15).px; top = 20.px }
            backButton()
        }
    }
}

private val backButton = FC<Props> {
    Link {
        to = "/tribes"
        tabIndex = -1
        draggable = false
        add(CouplingButton(large, blue, ClassName(""), {})) {
            i { className = ClassName("fa fa-step-backward") }
            span { +"Back to Coupling!" }
        }
    }
}

private val playerHeader = FC<Props> {
    div {
        val rob by playerImage()
        add(PlayerCard(Player("1", name = "RoB", imageURL = rob), tilt = (-8).deg)) {
            key = player.id
        }
        val autumn by playerImage()
        add(PlayerCard(Player("2", name = "Autumn", imageURL = autumn), tilt = 8.deg)) {
            key = player.id
        }
    }
}
