package com.zegreatrob.coupling.client.pairassignments

import com.zegreatrob.coupling.client.Controls
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
import com.zegreatrob.coupling.client.player.TinyPlayerList
import com.zegreatrob.coupling.client.party.PartyBrowser
import com.zegreatrob.coupling.client.user.ServerMessage
import com.zegreatrob.coupling.model.CouplingSocketMessage
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.party.Party
import com.zegreatrob.coupling.repository.pairassignmentdocument.PairAssignmentDocumentRepository
import com.zegreatrob.minreact.DataPropsBind
import com.zegreatrob.minreact.child
import com.zegreatrob.minreact.tmFC
import csstype.ClassName
import kotlinx.browser.window
import kotlinx.css.*
import kotlinx.css.properties.boxShadow
import kotlinx.html.tabIndex
import org.w3c.dom.DataTransfer
import org.w3c.dom.Node
import react.ChildrenBuilder
import react.MutableRefObject
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.i
import react.ref
import react.router.dom.Link
import react.useRef
import kotlin.js.Json
import kotlin.js.json

interface PairAssignmentsCommandDispatcher : SavePairAssignmentsCommandDispatcher,
    DeletePairAssignmentsCommandDispatcher {
    override val pairAssignmentDocumentRepository: PairAssignmentDocumentRepository
}

data class PairAssignments(
    val party: Party,
    val players: List<Player>,
    val pairAssignments: PairAssignmentDocument?,
    val setPairAssignments: (PairAssignmentDocument) -> Unit,
    val controls: Controls<DeletePairAssignmentsCommandDispatcher>,
    val message: CouplingSocketMessage,
    val allowSave: Boolean
) : DataPropsBind<PairAssignments>(com.zegreatrob.coupling.client.pairassignments.pairAssignments)

private val styles = useStyles("pairassignments/PairAssignments")

val pairAssignments = tmFC<PairAssignments> { props ->
    val (party, players, pairAssignments, setPairs, controls, message, allowSave) = props

    val pairSectionNode = useRef<Node>(null)

    DndProvider {
        backend = HTML5Backend
        div {
            className = styles.className
            div {
                child(PartyBrowser(party))
                topPairSection(party, players, pairAssignments, setPairs, allowSave, controls, pairSectionNode)
            }
            controlPanel(party)
            unpairedPlayerSection(party, notPairedPlayers(players, pairAssignments))

            child(ServerMessage(message), key = "${message.text} ${message.players.size}")
        }
    }
}

private fun ChildrenBuilder.topPairSection(
    party: Party,
    players: List<Player>,
    pairAssignments: PairAssignmentDocument?,
    setPairs: (PairAssignmentDocument) -> Unit,
    allowSave: Boolean,
    controls: Controls<DeletePairAssignmentsCommandDispatcher>,
    pairSectionNode: MutableRefObject<Node>
) = cssDiv(css = { verticalAlign = VerticalAlign.top }) {
    currentPairSection(
        party,
        players,
        pairAssignments?.overlayUpdatedPlayers(players),
        setPairs,
        allowSave,
        controls,
        pairSectionNode
    )
    cssDiv(css = { float = Float.right; width = 0.px }) {
        div { copyToClipboardButton(pairSectionNode) }

        child(TinyPlayerList(party, players))
    }
}

private fun PairAssignmentDocument.overlayUpdatedPlayers(players: List<Player>) = copy(pairs = pairs.map { pair ->
    pair.copy(players = pair.players.map { pinnedPlayer ->
        pinnedPlayer.copy(player = players.firstOrNull { p -> p.id == pinnedPlayer.player.id }
            ?: pinnedPlayer.player)
    })
})

private fun ChildrenBuilder.currentPairSection(
    party: Party,
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
        child(pairAssignmentsAnimator(party, players, pairAssignments, allowSave, setPairAssignments, controls))
    }
}

private fun pairAssignmentsAnimator(
    party: Party,
    players: List<Player>,
    pairAssignments: PairAssignmentDocument,
    allowSave: Boolean,
    setPairAssignments: (PairAssignmentDocument) -> Unit,
    controls: Controls<DeletePairAssignmentsCommandDispatcher>
) = PairAssignmentsAnimator(party, players, pairAssignments, enabled = party.animationEnabled && allowSave) {
    child(CurrentPairAssignmentsPanel(party, pairAssignments, setPairAssignments, allowSave, controls.dispatchFunc))
}

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

private fun ChildrenBuilder.controlPanel(party: Party) = div {
    div {
        className = styles["controlPanel"]
        div { prepareToSpinButton(party, styles["newPairsButton"]) }
    }
}

private fun ChildrenBuilder.copyToClipboardButton(ref: MutableRefObject<Node>) {
    if (js("!!global.ClipboardItem").unsafeCast<Boolean>()) {
        child(
            CouplingButton(
                large,
                white,
                styles["copyToClipboardButton"],
                onClick = ref.current?.copyToClipboardOnClick() ?: {},
                attrs = { tabIndex = "-1" })
        ) {
            i { className = ClassName("fa fa-clipboard") }
        }
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

private fun ChildrenBuilder.unpairedPlayerSection(party: Party, players: List<Player>) =
    child(PlayerRoster(label = "Unpaired players", players = players, partyId = party.id))

private fun notPairedPlayers(players: List<Player>, pairAssignments: PairAssignmentDocument?) =
    if (pairAssignments == null) {
        players
    } else {
        val currentlyPairedPlayerIds = pairAssignments.currentlyPairedPlayerIds()
        players.filterNot { player -> currentlyPairedPlayerIds.contains(player.id) }
    }

private fun PairAssignmentDocument.currentlyPairedPlayerIds() = pairs.flatMap { it.players }.map { it.player.id }

private fun ChildrenBuilder.prepareToSpinButton(party: Party, className: ClassName) = Link {
    to = "/${party.id.value}/prepare/"
    tabIndex = -1
    draggable = false
    child(CouplingButton(supersize, pink, className)) {
        +"Prepare to spin!"
    }
}
