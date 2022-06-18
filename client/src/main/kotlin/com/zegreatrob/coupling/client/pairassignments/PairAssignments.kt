package com.zegreatrob.coupling.client.pairassignments

import com.zegreatrob.coupling.client.Controls
import com.zegreatrob.coupling.client.dom.CouplingButton
import com.zegreatrob.coupling.client.dom.large
import com.zegreatrob.coupling.client.dom.pink
import com.zegreatrob.coupling.client.dom.supersize
import com.zegreatrob.coupling.client.dom.white
import com.zegreatrob.coupling.client.external.domtoimage.domToImage
import com.zegreatrob.coupling.client.external.react.get
import com.zegreatrob.coupling.client.external.react.useStyles
import com.zegreatrob.coupling.client.external.reactdnd.DndProvider
import com.zegreatrob.coupling.client.external.reactdndhtml5backend.HTML5Backend
import com.zegreatrob.coupling.client.pairassignments.list.DeletePairAssignmentsCommandDispatcher
import com.zegreatrob.coupling.client.pairassignments.spin.PairAssignmentsAnimator
import com.zegreatrob.coupling.client.party.PartyBrowser
import com.zegreatrob.coupling.client.player.PlayerRoster
import com.zegreatrob.coupling.client.player.TinyPlayerList
import com.zegreatrob.coupling.client.user.ServerMessage
import com.zegreatrob.coupling.model.CouplingSocketMessage
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.model.party.Party
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.repository.pairassignmentdocument.PairAssignmentDocumentRepository
import com.zegreatrob.minreact.DataPropsBind
import com.zegreatrob.minreact.create
import com.zegreatrob.minreact.tmFC
import csstype.Border
import csstype.BoxShadow
import csstype.ClassName
import csstype.Display
import csstype.FontSize
import csstype.FontWeight
import csstype.LineStyle
import csstype.Margin
import csstype.NamedColor
import csstype.Padding
import csstype.VerticalAlign
import csstype.px
import csstype.rgb
import csstype.rgba
import emotion.react.css
import kotlinx.browser.window
import org.w3c.dom.DataTransfer
import org.w3c.dom.Node
import react.ChildrenBuilder
import react.MutableRefObject
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.i
import react.key
import react.router.dom.Link
import react.useRef
import kotlin.js.Json
import kotlin.js.json

interface PairAssignmentsCommandDispatcher :
    SavePairAssignmentsCommandDispatcher,
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
                +PartyBrowser(party).create()
                topPairSection(party, players, pairAssignments, setPairs, allowSave, controls, pairSectionNode)
            }
            controlPanel(party)
            unpairedPlayerSection(party, notPairedPlayers(players, pairAssignments))

            +ServerMessage(message).create {
                key = "${message.text} ${message.players.size}"
            }
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
) = div {
    css { verticalAlign = VerticalAlign.top }
    currentPairSection(
        party,
        players,
        pairAssignments?.overlayUpdatedPlayers(players),
        setPairs,
        allowSave,
        controls,
        pairSectionNode
    )
    div {
        css { float = csstype.Float.right; width = 0.px }
        div { copyToClipboardButton(pairSectionNode) }
        +TinyPlayerList(party, players).create()
    }
}

private fun PairAssignmentDocument.overlayUpdatedPlayers(players: List<Player>) = copy(
    pairs = pairs.map { pair ->
        pair.copy(
            players = pair.players.map { pinnedPlayer ->
                pinnedPlayer.copy(
                    player = players.firstOrNull { p -> p.id == pinnedPlayer.player.id }
                        ?: pinnedPlayer.player
                )
            }
        )
    }
)

private fun ChildrenBuilder.currentPairSection(
    party: Party,
    players: List<Player>,
    pairAssignments: PairAssignmentDocument?,
    setPairAssignments: (PairAssignmentDocument) -> Unit,
    allowSave: Boolean,
    controls: Controls<DeletePairAssignmentsCommandDispatcher>,
    pairSectionNode: MutableRefObject<Node>
) = div {
    ref = pairSectionNode
    css {
        display = Display.inlineBlock
        borderRadius = 20.px
        padding = 5.px
        margin = Margin(5.px, 0.px)
        backgroundColor = rgb(195, 213, 203)
        boxShadow = BoxShadow(1.px, 1.px, 3.px, rgba(0, 0, 0, 0.6))
    }
    if (pairAssignments == null) {
        noPairsHeader()
    } else {
        +pairAssignmentsAnimator(party, players, pairAssignments, allowSave, setPairAssignments, controls).create()
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
    +CurrentPairAssignmentsPanel(party, pairAssignments, setPairAssignments, allowSave, controls.dispatchFunc).create()
}

private fun ChildrenBuilder.noPairsHeader() = div {
    css {
        border = Border(8.px, LineStyle.outset, NamedColor.dimgray)
        backgroundColor = NamedColor.aliceblue
        display = Display.inlineBlock
        borderRadius = 40.px
        fontSize = FontSize.xxLarge
        fontWeight = FontWeight.bold
        width = 500.px
        height = 150.px
        padding = Padding(100.px, 5.px, 5.px)
        margin = Margin(0.px, 2.px, 5.px)
    }
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
        +CouplingButton(
            large,
            white,
            styles["copyToClipboardButton"],
            onClick = ref.current?.copyToClipboardOnClick() ?: {},
            attrs = { tabIndex = -1 }
        ).create {
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
    +PlayerRoster(label = "Unpaired players", players = players, partyId = party.id).create()

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
    +CouplingButton(supersize, pink, className).create {
        +"Prepare to spin!"
    }
}
