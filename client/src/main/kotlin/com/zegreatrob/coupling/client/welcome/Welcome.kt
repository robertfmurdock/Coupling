package com.zegreatrob.coupling.client.welcome

import com.zegreatrob.coupling.client.dom.CouplingButton
import com.zegreatrob.coupling.client.dom.pink
import com.zegreatrob.coupling.client.dom.supersize
import com.zegreatrob.coupling.client.external.react.get
import com.zegreatrob.coupling.client.external.react.useStyles
import com.zegreatrob.coupling.client.fitty.fitty
import com.zegreatrob.coupling.client.player.PlayerCard
import com.zegreatrob.coupling.client.playerImage
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
import emotion.css.ClassName
import emotion.react.css
import org.w3c.dom.Node
import react.ChildrenBuilder
import react.MutableRefObject
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.span
import react.useLayoutEffect
import react.useRef
import react.useState

private val styles = useStyles("Welcome")

data class Welcome(val randomProvider: RandomProvider = RandomProvider) : DataPropsBind<Welcome>(welcome)

val welcome = tmFC { (randomProvider): Welcome ->
    var showLoginChooser by useState(false)
    val welcomeTitleRef = useRef<Node>(null)
    useLayoutEffect {
        welcomeTitleRef.current?.fitty(maxFontHeight = 75.0, minFontHeight = 5.0, multiLine = false)
    }
    val (pairAndProverb) = useState { randomProvider.choosePairAndProverb() }
    val (pair, proverb) = pairAndProverb

    div {
        css(styles.className) { welcomeStyles() }
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

private val candidates = listOf(
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

private data class WelcomeCardSet(val left: Card, val right: Card, val proverb: String)

private data class Card(val name: String, val imagePath: String)

private fun ChildrenBuilder.welcomeSplash(
    welcomeTitleRef: MutableRefObject<Node>,
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
        css(styles["welcomeProverb"]) {
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

private fun ChildrenBuilder.welcomeTitle(welcomeTitleRef: MutableRefObject<Node>) = div {
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
        val leftCardStyles = ClassName(styles["playerCard"]) {
            transitionProperty = TransitionProperty.all
            transitionDuration = 0.25.s
            transitionTimingFunction = TransitionTimingFunction.easeOut
            animationDuration = 4.s
            animationName = ident("slide-up-left")
            animationIterationCount = number(1.0)
            hover { cardZoom() }
        }
        add(PlayerCard(pair.player1, leftCardStyles, 100, tilt = (-8).deg))
        val rightCardStyles = ClassName(styles["playerCard"]) {
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
    css(styles["enterButtonContainer"]) {
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
                ClassName(styles["enterButton"]) {
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
