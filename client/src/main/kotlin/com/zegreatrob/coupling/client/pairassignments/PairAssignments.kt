package com.zegreatrob.coupling.client.pairassignments

import com.zegreatrob.coupling.client.Controls
import com.zegreatrob.coupling.client.create
import com.zegreatrob.coupling.client.cssDiv
import com.zegreatrob.coupling.client.dom.*
import com.zegreatrob.coupling.client.external.domtoimage.domToImage
import com.zegreatrob.coupling.client.external.react.get
import com.zegreatrob.coupling.client.external.react.useStyles
import com.zegreatrob.coupling.client.external.reactdnd.DndProvider
import com.zegreatrob.coupling.client.external.reactdndhtml5backend.HTML5Backend
import com.zegreatrob.coupling.client.pairassignments.list.DeletePairAssignmentsCommandDispatcher
import com.zegreatrob.coupling.client.pairassignments.spin.PairAssignmentsAnimator
import com.zegreatrob.coupling.client.player.PlayerRoster
import com.zegreatrob.coupling.client.tribe.TribeBrowser
import com.zegreatrob.coupling.client.user.ServerMessage
import com.zegreatrob.coupling.model.CouplingSocketMessage
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.tribe.Tribe
import com.zegreatrob.coupling.repository.pairassignmentdocument.PairAssignmentDocumentRepository
import com.zegreatrob.minreact.DataProps
import com.zegreatrob.minreact.TMFC
import com.zegreatrob.minreact.child
import com.zegreatrob.minreact.tmFC
import kotlinx.browser.window
import kotlinx.css.*
import kotlinx.css.properties.boxShadow
import kotlinx.html.tabIndex
import org.w3c.dom.DataTransfer
import org.w3c.dom.Node
import react.*
import react.dom.attrs
import react.dom.html.ReactHTML.div
import react.dom.i
import react.router.dom.Link
import kotlin.js.Json
import kotlin.js.json

interface PairAssignmentsCommandDispatcher : SavePairAssignmentsCommandDispatcher,
    DeletePairAssignmentsCommandDispatcher {
    override val pairAssignmentDocumentRepository: PairAssignmentDocumentRepository
}

data class PairAssignments(
    val tribe: Tribe,
    val players: List<Player>,
    val pairAssignments: PairAssignmentDocument?,
    val setPairAssignments: (PairAssignmentDocument) -> Unit,
    val controls: Controls<DeletePairAssignmentsCommandDispatcher>,
    val message: CouplingSocketMessage,
    val allowSave: Boolean
) : DataProps<PairAssignments> {
    override val component: TMFC<PairAssignments> get() = com.zegreatrob.coupling.client.pairassignments.pairAssignments
}

private val styles = useStyles("pairassignments/PairAssignments")

val pairAssignments = tmFC<PairAssignments> { props ->
    val (tribe, players, pairAssignments, setPairs, controls, message, allowSave) = props

    val pairSectionNode = useRef<Node>(null)

    DndProvider {
        backend = HTML5Backend
        div {
            className = styles.className
            div {
                child(TribeBrowser(tribe))
                topPairSection(tribe, players, pairAssignments, setPairs, allowSave, controls, pairSectionNode)
            }
            +controlPanel(tribe)
            +unpairedPlayerSection(tribe, notPairedPlayers(players, pairAssignments))

            child(ServerMessage(tribe.id, message), key = "${message.text} ${message.players.size}")
        }
    }
}

private fun ChildrenBuilder.topPairSection(
    tribe: Tribe,
    players: List<Player>,
    pairAssignments: PairAssignmentDocument?,
    setPairs: (PairAssignmentDocument) -> Unit,
    allowSave: Boolean,
    controls: Controls<DeletePairAssignmentsCommandDispatcher>,
    pairSectionNode: MutableRefObject<Node>
) = cssDiv(css = { verticalAlign = VerticalAlign.top }) {
    currentPairSection(
        tribe,
        players,
        pairAssignments,
        setPairs,
        allowSave,
        controls,
        pairSectionNode
    )
    cssDiv(css = { float = Float.right; width = 0.px }) {
        copyToClipboardButton(pairSectionNode)?.let(::child)
    }
}


private fun ChildrenBuilder.currentPairSection(
    tribe: Tribe,
    players: List<Player>,
    pairAssignments: PairAssignmentDocument?,
    setPairAssignments: (PairAssignmentDocument) -> Unit,
    allowSave: Boolean,
    controls: Controls<DeletePairAssignmentsCommandDispatcher>,
    pairSectionNode: MutableRefObject<Node>
) = cssDiv(
    props = { ref = pairSectionNode },
    css = {
        display = Display.inlineBlock
        borderRadius = 20.px
        padding(5.px)
        margin(5.px, 0.px)
        backgroundColor = hsla(146, 17, 80, 1.0)
        boxShadow(rgba(0, 0, 0, 0.6), 1.px, 1.px, 3.px)
    }) {
    if (pairAssignments == null) {
        noPairsHeader()
    } else {
        +pairAssignmentsAnimator(tribe, players, pairAssignments, allowSave, setPairAssignments, controls)
    }
}

private fun pairAssignmentsAnimator(
    tribe: Tribe,
    players: List<Player>,
    pairAssignments: PairAssignmentDocument,
    allowSave: Boolean,
    setPairAssignments: (PairAssignmentDocument) -> Unit,
    controls: Controls<DeletePairAssignmentsCommandDispatcher>
) = PairAssignmentsAnimator(tribe, players, pairAssignments, enabled = tribe.animationEnabled && allowSave) {
    child(CurrentPairAssignmentsPanel(tribe, pairAssignments, setPairAssignments, allowSave, controls.dispatchFunc))
}.create()

private fun ChildrenBuilder.noPairsHeader() = cssDiv(css = {
    border = "8px outset dimgray"
    backgroundColor = Color.aliceBlue
    display = Display.inlineBlock
    borderRadius = 40.px
    fontSize = LinearDimension("xx-large")
    fontWeight = FontWeight.bold
    width = 500.px
    height = 150.px
    padding(100.px, 5.px, 5.px)
    margin(0.px, 2.px, 5.px)
}) {
    +"No pair assignments yet!"
}

private fun controlPanel(tribe: Tribe) = div.create {
    div {
        className = styles["controlPanel"]
        div { +prepareToSpinButton(tribe, styles["newPairsButton"]) }
        +viewHistoryButton(tribe, styles["viewHistoryButton"])
        +pinListButton(tribe, styles["pinListButton"])
        +statisticsButton(tribe, styles["statisticsButton"])
        +viewRetireesButton(tribe, styles["retiredPlayersButton"])
    }
}

private fun copyToClipboardButton(ref: MutableRefObject<Node>): ReactElement? = ref.current?.let { node ->
    if (!js("!!global.ClipboardItem").unsafeCast<Boolean>()) {
        null
    } else {
        CouplingButton(large,
            black,
            styles["copyToClipboardButton"],
            onClick = node.copyToClipboardOnClick(),
            block = { attrs { tabIndex = "-1" } }) { i(classes = "fa fa-clipboard") {} }
            .create()
    }
}

private fun Node.copyToClipboardOnClick(): () -> Unit = if (isReallyTrulySafari())
    writeImageToClipboardAsPromise()
else
    collectImageThenWriteToClipboard()

private fun Node.collectImageThenWriteToClipboard(): () -> Unit = {
    domToImage.toBlob(this).then { window.navigator.clipboard.write(dataTransfer(it)) }
}

private fun Node.writeImageToClipboardAsPromise(): () -> Unit = {
    window.navigator.clipboard.write(dataTransfer(domToImage.toBlob(this)))
}

private fun isReallyTrulySafari() = window.navigator.userAgent.indexOf("Safari") != -1 &&
        window.navigator.userAgent.indexOf("Chrome") == -1

private fun dataTransfer(it: Any) = arrayOf(ClipboardItem(json("image/png" to it))).unsafeCast<DataTransfer>()

external class ClipboardItem(params: Json)

private fun unpairedPlayerSection(tribe: Tribe, players: List<Player>) =
    PlayerRoster(label = "Unpaired players", players = players, tribeId = tribe.id)
        .create()

private fun notPairedPlayers(players: List<Player>, pairAssignments: PairAssignmentDocument?) =
    if (pairAssignments == null) {
        players
    } else {
        val currentlyPairedPlayerIds = pairAssignments.currentlyPairedPlayerIds()
        players.filterNot { player -> currentlyPairedPlayerIds.contains(player.id) }
    }

private fun PairAssignmentDocument.currentlyPairedPlayerIds() = pairs.flatMap { it.players }.map { it.player.id }

private fun prepareToSpinButton(tribe: Tribe, className: String) = Link.create {
    to = "/${tribe.id.value}/prepare/"
    child(CouplingButton(supersize, pink, className, {}, {}) { +"Prepare to spin!" })
}

private fun viewHistoryButton(tribe: Tribe, className: String) = Link.create {
    to = "/${tribe.id.value}/history/"
    child(CouplingButton(large, green, className, {}, {}) {
        i(classes = "fa fa-history") {}
        +" History!"
    })
}

private fun pinListButton(tribe: Tribe, className: String) = Link.create {
    to = "/${tribe.id.value}/pins/"
    child(CouplingButton(large, white, className, {}, {}) {
        i(classes = "fa fa-peace") {}
        +" Pin Bag!"
    })
}

private fun statisticsButton(tribe: Tribe, className: String) = Link.create {
    to = "/${tribe.id.value}/statistics"
    child(CouplingButton(large, className = className) {
        i(classes = "fa fa-database") {}
        +" Statistics!"
    })
}

private fun viewRetireesButton(tribe: Tribe, className: String) = Link.create {
    to = "/${tribe.id.value}/players/retired"
    child(CouplingButton(large, yellow, className, {}, {}) {
        i(classes = "fa fa-user-slash") {}
        +" Retirees!"
    })
}
