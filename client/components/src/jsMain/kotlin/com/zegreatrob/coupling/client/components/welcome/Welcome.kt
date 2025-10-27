package com.zegreatrob.coupling.client.components.welcome

import com.zegreatrob.coupling.client.components.CouplingButton
import com.zegreatrob.coupling.client.components.CouplingImages
import com.zegreatrob.coupling.client.components.external.fitty.fitty
import com.zegreatrob.coupling.client.components.pink
import com.zegreatrob.coupling.client.components.player.PlayerCard
import com.zegreatrob.coupling.client.components.supersize
import com.zegreatrob.coupling.model.pairassignmentdocument.CouplingPair
import com.zegreatrob.coupling.model.pairassignmentdocument.pairOf
import com.zegreatrob.coupling.model.player.PlayerId
import com.zegreatrob.coupling.model.player.defaultPlayer
import com.zegreatrob.minreact.ReactFunc
import com.zegreatrob.minreact.nfc
import csstype.PropertiesBuilder
import emotion.css.ClassName
import emotion.react.css
import kotools.types.text.toNotBlankString
import react.ChildrenBuilder
import react.Props
import react.RefObject
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.span
import react.useLayoutEffect
import react.useRef
import react.useState
import web.cssom.AnimationIterationCount
import web.cssom.AnimationPlayState
import web.cssom.Border
import web.cssom.Color
import web.cssom.Display
import web.cssom.FontWeight
import web.cssom.LineStyle
import web.cssom.NamedColor
import web.cssom.Overflow
import web.cssom.Padding
import web.cssom.TransformFunction
import web.cssom.TransitionProperty
import web.cssom.TransitionTimingFunction
import web.cssom.VerticalAlign
import web.cssom.deg
import web.cssom.em
import web.cssom.ident
import web.cssom.integer
import web.cssom.number
import web.cssom.px
import web.cssom.rotate
import web.cssom.s
import web.html.HTMLDivElement

external interface WelcomeProps : Props {
    var randomProvider: RandomProvider?
}

@ReactFunc
val Welcome by nfc<WelcomeProps> { props ->
    val randomProvider = props.randomProvider ?: RandomProvider
    var showLoginChooser by useState(false)
    val welcomeTitleRef = useRef<HTMLDivElement>(null)
    useLayoutEffect {
        welcomeTitleRef.current?.fitty(maxFontHeight = 75.0, minFontHeight = 5.0, multiLine = false)
    }
    val (pairAndProverb) = useState { randomProvider.choosePairAndProverb() }
    val (pair, proverb) = pairAndProverb

    div {
        css { welcomeStyles() }
        div { welcomeSplash(welcomeTitleRef, pair, proverb) }
        div { comeOnIn(showLoginChooser) { showLoginChooser = true } }
    }
}

private fun PropertiesBuilder.welcomeStyles() {
    display = Display.inlineBlock
    overflow = Overflow.hidden
    paddingTop = 75.px
    transitionProperty = TransitionProperty.all
    transitionDuration = 4.s
    transitionTimingFunction = TransitionTimingFunction.easeOut
    animationName = ident("curious-tilt")
    animationDuration = 4.s
    animationIterationCount = number(1.0)
}

private val candidates by lazy {
    listOf(
        WelcomeCardSet(
            left = Card(name = "Frodo", imagePath = CouplingImages.images.frodo),
            right = Card(name = "Sam", imagePath = CouplingImages.images.samwise),
            proverb = "Together, climb mountains.",
        ),
        WelcomeCardSet(
            left = Card(name = "Batman", imagePath = CouplingImages.images.grayson),
            right = Card(name = "Robin", imagePath = CouplingImages.images.wayne),
            proverb = "Clean up the city, together.",
        ),
        WelcomeCardSet(
            left = Card(name = "Rosie", imagePath = CouplingImages.images.rosie),
            right = Card(name = "Wendy", imagePath = CouplingImages.images.wendy),
            proverb = "Team up. Get things done.",
        ),
    )
}

private data class WelcomeCardSet(val left: Card, val right: Card, val proverb: String)

private data class Card(val name: String, val imagePath: String)

private fun ChildrenBuilder.welcomeSplash(
    welcomeTitleRef: RefObject<HTMLDivElement>,
    pair: CouplingPair.Double,
    proverb: String,
) = span {
    css {
        display = Display.inlineBlock
        verticalAlign = VerticalAlign.top
        borderRadius = 82.px
        padding = Padding(0.px, 42.px, 18.px)
        border = Border(24.px, LineStyle.solid, NamedColor.darkorange)
        backgroundColor = Color("#faf0d2")
    }
    welcomeTitle(welcomeTitleRef)
    welcomePair(pair)
    welcomeProverb(proverb)
}

private fun ChildrenBuilder.welcomeProverb(proverb: String) {
    div {
        css {
            display = Display.inlineBlock
            borderRadius = 15.px
            transform = rotate(0.deg)
            padding = 4.px
            marginTop = 20.px
            transitionDelay = 5.s
            transitionProperty = TransitionProperty.all
            transitionTimingFunction = TransitionTimingFunction.easeIn
            transitionDuration = 1.s
        }
        +proverb
    }
}

private fun RandomProvider.choosePairAndProverb() = chooseWelcomeCardSet().toPairAndProverb()

private fun WelcomeCardSet.toPairAndProverb() = pairOf(
    left.toPlayer(),
    right.toPlayer(),
) to proverb

private fun RandomProvider.chooseWelcomeCardSet() = candidates.random()

private fun Card.toPlayer() = defaultPlayer.copy(id = PlayerId(name.toNotBlankString().getOrThrow()), name = name, imageURL = imagePath)

private fun ChildrenBuilder.welcomeTitle(welcomeTitleRef: RefObject<HTMLDivElement>) = div {
    css {
        marginTop = 0.5.em
        marginBottom = 0.4.em
        fontSize = 72.px
        fontWeight = FontWeight.bold
    }
    ref = welcomeTitleRef
    +"Coupling!"
}

private fun ChildrenBuilder.welcomePair(pair: CouplingPair.Double) = div {
    div {
        css {
            display = Display.inlineFlex
        }
        val leftCardStyles = ClassName {
            transitionProperty = TransitionProperty.all
            transitionDuration = 0.25.s
            transitionTimingFunction = TransitionTimingFunction.easeOut
            animationDuration = 4.s
            animationName = ident("slide-up-left")
            animationIterationCount = number(1.0)
            hover { cardZoom() }
        }
        PlayerCard(pair.player1, leftCardStyles, size = 100, tilt = (-8).deg, key = "player-1")
        val rightCardStyles = ClassName {
            transitionProperty = TransitionProperty.all
            transitionDuration = 0.25.s
            transitionTimingFunction = TransitionTimingFunction.easeOut
            animationDuration = 5.s
            animationName = ident("slide-up-right")
            animationIterationCount = number(1.0)
            hover { cardZoom() }
        }
        PlayerCard(pair.player2, rightCardStyles, size = 100, tilt = 8.deg, key = "player-2")
    }
}

private fun PropertiesBuilder.cardZoom() {
    transitionProperty = TransitionProperty.all
    transitionDuration = 0.4.s
    transitionTimingFunction = TransitionTimingFunction.easeIn
    zIndex = integer(1)
    transform = "rotate(0deg) scale(1.5)".unsafeCast<TransformFunction>()
}

private fun ChildrenBuilder.comeOnIn(showLoginChooser: Boolean, onEnterClick: () -> Unit) = div {
    css {
        paddingTop = 15.px
        paddingBottom = 50.px
        display = Display.inlineBlock
        animationDuration = 2.s
        animationName = ident("pulsate")
        animationIterationCount = AnimationIterationCount.infinite

        hover {
            animationPlayState = AnimationPlayState.paused
        }
    }
    if (showLoginChooser) {
        LoginChooser()
    } else {
        CouplingButton {
            sizeRuleSet = supersize
            colorRuleSet = pink
            css {
                marginTop = 20.px
                animationName = ident("pulsate")
                animationDuration = 0.5.s
                animationIterationCount = AnimationIterationCount.infinite
                animationPlayState = AnimationPlayState.paused
                hover {
                    animationPlayState = AnimationPlayState.running
                }
            }
            onClick = onEnterClick
            +"Come on in!"
        }
    }
}
