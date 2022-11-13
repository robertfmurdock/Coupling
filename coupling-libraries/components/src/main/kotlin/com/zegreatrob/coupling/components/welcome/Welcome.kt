package com.zegreatrob.coupling.components.welcome

import com.zegreatrob.coupling.components.CouplingButton
import com.zegreatrob.coupling.components.PlayerCard
import com.zegreatrob.coupling.components.external.fitty.fitty
import com.zegreatrob.coupling.components.pink
import com.zegreatrob.coupling.components.supersize
import com.zegreatrob.coupling.model.pairassignmentdocument.CouplingPair
import com.zegreatrob.coupling.model.pairassignmentdocument.pairOf
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.minreact.DataPropsBind
import com.zegreatrob.minreact.add
import com.zegreatrob.minreact.tmFC
import csstype.AnimationIterationCount
import csstype.AnimationPlayState
import csstype.Border
import csstype.Color
import csstype.Display
import csstype.FontWeight
import csstype.LineStyle
import csstype.NamedColor
import csstype.Overflow
import csstype.Padding
import csstype.PropertiesBuilder
import csstype.TransformFunction
import csstype.TransitionProperty
import csstype.TransitionTimingFunction
import csstype.VerticalAlign
import csstype.deg
import csstype.em
import csstype.ident
import csstype.integer
import csstype.number
import csstype.px
import csstype.rotate
import csstype.s
import dom.html.HTMLDivElement
import emotion.css.ClassName
import emotion.react.css
import react.ChildrenBuilder
import react.MutableRefObject
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.span
import react.useLayoutEffect
import react.useRef
import react.useState

data class Welcome(val randomProvider: RandomProvider = RandomProvider) : DataPropsBind<Welcome>(welcome)

val welcome = tmFC { (randomProvider): Welcome ->
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

val frodo by playerImage()
val samwise by playerImage()
val grayson by playerImage()
val wayne by playerImage()
val rosie by playerImage()
val wendy by playerImage()

private val candidates by lazy {
    listOf(
        WelcomeCardSet(
            left = Card(name = "Frodo", imagePath = frodo),
            right = Card(name = "Sam", imagePath = samwise),
            proverb = "Together, climb mountains."
        ),
        WelcomeCardSet(
            left = Card(name = "Batman", imagePath = grayson),
            right = Card(name = "Robin", imagePath = wayne),
            proverb = "Clean up the city, together."
        ),
        WelcomeCardSet(
            left = Card(name = "Rosie", imagePath = rosie),
            right = Card(name = "Wendy", imagePath = wendy),
            proverb = "Team up. Get things done."
        )
    )
}

private data class WelcomeCardSet(val left: Card, val right: Card, val proverb: String)

private data class Card(val name: String, val imagePath: String)

private fun ChildrenBuilder.welcomeSplash(
    welcomeTitleRef: MutableRefObject<HTMLDivElement>,
    pair: CouplingPair.Double,
    proverb: String
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
    right.toPlayer()
) to proverb

private fun RandomProvider.chooseWelcomeCardSet() = candidates.random()

private fun Card.toPlayer() = Player(id = name, name = name, imageURL = imagePath)

private fun ChildrenBuilder.welcomeTitle(welcomeTitleRef: MutableRefObject<HTMLDivElement>) = div {
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
        add(PlayerCard(pair.player1, leftCardStyles, 100, tilt = (-8).deg))
        val rightCardStyles = ClassName {
            transitionProperty = TransitionProperty.all
            transitionDuration = 0.25.s
            transitionTimingFunction = TransitionTimingFunction.easeOut
            animationDuration = 5.s
            animationName = ident("slide-up-right")
            animationIterationCount = number(1.0)
            hover { cardZoom() }
        }
        add(PlayerCard(pair.player2, rightCardStyles, 100, tilt = 8.deg))
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
        add(
            CouplingButton(
                supersize, pink,
                ClassName {
                    marginTop = 20.px
                    animationName = ident("pulsate")
                    animationDuration = 0.5.s
                    animationIterationCount = AnimationIterationCount.infinite
                    animationPlayState = AnimationPlayState.paused
                    hover {
                        animationPlayState = AnimationPlayState.running
                    }
                },
                onEnterClick
            )
        ) { +"Come on in!" }
    }
}
