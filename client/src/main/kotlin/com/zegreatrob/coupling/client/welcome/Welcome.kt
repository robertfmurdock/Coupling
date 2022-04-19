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
import com.zegreatrob.minreact.child
import com.zegreatrob.minreact.tmFC
import csstype.ClassName
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
        className = styles.className
        div { welcomeSplash(welcomeTitleRef, pair, proverb) }
        div { comeOnIn(showLoginChooser) { showLoginChooser = true } }
    }
}

val frodo by playerImage()
val samwise by playerImage()
val grayson by playerImage()
val wayne by playerImage()
val rosie by playerImage()
val wendy by playerImage()

private val candidates: List<WelcomeCardSet> = listOf(
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
    className = styles["welcome"]
    welcomeTitle(welcomeTitleRef)
    div { welcomePair(pair) }
    div {
        className = styles["welcomeProverb"]
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
    className = styles["welcomeTitle"]
    ref = welcomeTitleRef
    +"Coupling!"
}

private fun ChildrenBuilder.welcomePair(pair: CouplingPair.Double) = div {
    className = styles["welcomePair"]
    child(PlayerCard(pair.player1, className = ClassName("left ${styles["playerCard"]}"), size = 100))
    child(PlayerCard(pair.player2, className = ClassName("right ${styles["playerCard"]}"), size = 100))
}

private fun ChildrenBuilder.comeOnIn(showLoginChooser: Boolean, onEnterClick: () -> Unit) = div {
    className = styles["enterButtonContainer"]
    if (showLoginChooser) {
        LoginChooser()
    } else {
        child(CouplingButton(supersize, pink, styles["enterButton"], onEnterClick)) { +"Come on in!" }
    }
}
