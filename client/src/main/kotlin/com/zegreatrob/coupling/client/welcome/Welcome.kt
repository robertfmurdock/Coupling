package com.zegreatrob.coupling.client.welcome

import com.zegreatrob.coupling.client.DispatchFunc
import com.zegreatrob.coupling.client.dom.couplingButton
import com.zegreatrob.coupling.client.dom.pink
import com.zegreatrob.coupling.client.dom.supersize
import com.zegreatrob.coupling.client.external.react.get
import com.zegreatrob.coupling.client.external.react.useStyles
import com.zegreatrob.coupling.client.fitty.fitty
import com.zegreatrob.coupling.client.player.PlayerCardProps
import com.zegreatrob.coupling.client.player.playerCard
import com.zegreatrob.coupling.client.user.GoogleSignInCommandDispatcher
import com.zegreatrob.coupling.model.pairassignmentdocument.CouplingPair
import com.zegreatrob.coupling.model.pairassignmentdocument.pairOf
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.minreact.reactFunction
import kotlinx.html.js.onClickFunction
import org.w3c.dom.Node
import react.*
import react.dom.div
import react.dom.span

private val styles = useStyles("Welcome")

data class WelcomeProps(
    val dispatchFunc: DispatchFunc<out GoogleSignInCommandDispatcher>,
    val randomProvider: RandomProvider = RandomProvider
) : RProps

val Welcome = reactFunction { (commandFunc, randomProvider): WelcomeProps ->
    div(classes = styles.className) {
        div { welcomeSplash(randomProvider) }
        div { comeOnIn(commandFunc) }
    }
}

val welcomeTribeId = TribeId("welcome")

private val candidates = listOf(
    WelcomeCardSet(
        left = Card(name = "Frodo", imagePath = "frodo-icon.png"),
        right = Card(name = "Sam", imagePath = "samwise-icon.png"),
        proverb = "Together, climb mountains."
    ),
    WelcomeCardSet(
        left = Card(name = "Batman", imagePath = "grayson-icon.png"),
        right = Card(name = "Robin", imagePath = "wayne-icon.png"),
        proverb = "Clean up the city, together."
    ),
    WelcomeCardSet(
        left = Card(name = "Rosie", imagePath = "rosie-icon.png"),
        right = Card(name = "Wendy", imagePath = "wendy-icon.png"),
        proverb = "Team up. Get things done."
    )
)

private data class WelcomeCardSet(val left: Card, val right: Card, val proverb: String)

private data class Card(val name: String, val imagePath: String)

private fun RBuilder.welcomeSplash(randomProvider: RandomProvider) {
    val (pairAndProverb) = useState { randomProvider.choosePairAndProverb() }

    val (pair, proverb) = pairAndProverb

    span(classes = styles["welcome"]) {
        welcomeTitle()
        div { welcomePair(pair) }
        div(classes = styles["welcomeProverb"]) {
            +proverb
        }
    }
}

private fun RandomProvider.choosePairAndProverb() = chooseWelcomeCardSet().toPairAndProverb()

private fun WelcomeCardSet.toPairAndProverb() = pairOf(
    left.toPlayer(),
    right.toPlayer()
) to proverb

private fun RandomProvider.chooseWelcomeCardSet() = candidates.random()

private fun Card.toPlayer() = Player(
    id = name,
    name = name,
    imageURL = "/images/icons/players/$imagePath"
)

private fun RBuilder.welcomeTitle() {
    val welcomeTitleRef = useRef<Node?>(null)
    useLayoutEffect {
        welcomeTitleRef.current?.fitty(maxFontHeight = 75.0, minFontHeight = 5.0, multiLine = false)
    }
    div(classes = styles["welcomeTitle"]) {
        attrs { ref = welcomeTitleRef }
        +"Coupling!"
    }
}

private fun RBuilder.welcomePair(pair: CouplingPair.Double) = div(classes = styles["welcomePair"]) {
    playerCard(
        PlayerCardProps(
            tribeId = welcomeTribeId,
            player = pair.player1,
            className = "left ${styles["playerCard"]}",
            size = 100
        )
    )
    playerCard(
        PlayerCardProps(
            tribeId = welcomeTribeId,
            player = pair.player2,
            className = "right ${styles["playerCard"]}",
            size = 100
        )
    )
}

private fun RBuilder.comeOnIn(dispatchFunc: DispatchFunc<out GoogleSignInCommandDispatcher>) {
    val (showLoginChooser, setShowLoginChooser) = useState(false)
    div(classes = styles["enterButtonContainer"]) {
        if (showLoginChooser) {
            loginChooser(dispatchFunc)
        } else {
            couplingButton(supersize, pink, "enter-button ${styles["enterButton"]}") {
                attrs {
                    onClickFunction = { setShowLoginChooser(true) }
                }
                +"Come on in!"
            }
        }
    }
}