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
import com.zegreatrob.minreact.add
import com.zegreatrob.minreact.tmFC
import csstype.AnimationIterationCount
import csstype.BoxShadow
import csstype.ClassName
import csstype.Color
import csstype.Display
import csstype.Float
import csstype.Margin
import csstype.VerticalAlign
import csstype.ident
import csstype.px
import csstype.rgba
import csstype.s
import emotion.react.css
import kotlinx.browser.window
import org.w3c.dom.DataTransfer
import org.w3c.dom.Node
import react.ChildrenBuilder
import react.MutableRefObject
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.i
import react.key
import react.ref
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
    val (party, players, pairs, setPairs, controls, message, allowSave) = props

    val pairAssignments = pairs?.overlayUpdatedPlayers(players)
    val pairSectionNode = useRef<Node>(null)

    DndProvider {
        backend = HTML5Backend
        div {
            className = styles.className
            div {
                add(PartyBrowser(party))
                div {
                    css { verticalAlign = VerticalAlign.top }
                    add(PairSection(party, players, pairAssignments, allowSave, setPairs, controls)) {
                        ref = pairSectionNode
                    }
                    div {
                        css { float = Float.right; width = 0.px }
                        div { copyToClipboardButton(pairSectionNode) }
                        add(TinyPlayerList(party, players))
                    }
                }
            }
            add(ControlPanel(party))
            unpairedPlayerSection(party, notPairedPlayers(players, pairs))

            add(ServerMessage(message)) {
                key = "${message.text} ${message.players.size}"
            }
        }
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

data class ControlPanel(val party: Party) : DataPropsBind<ControlPanel>(controlPanel)

val controlPanel = tmFC<ControlPanel> { (party) ->
    div {
        div {
            css {
                display = Display.inlineBlock
                borderRadius = 20.px
                padding = 5.px
                margin = Margin(5.px, 0.px)
                backgroundColor = Color("#d5cdc3")
                boxShadow = BoxShadow(1.px, 1.px, 3.px, rgba(0, 0, 0, 0.6))
            }
            div { add(PrepareToSpinButton(party)) }
        }
    }
}

private fun ChildrenBuilder.copyToClipboardButton(ref: MutableRefObject<Node>) {
    if (js("!!global.ClipboardItem").unsafeCast<Boolean>()) {
        add(
            CouplingButton(
                sizeRuleSet = large,
                colorRuleSet = white,
                className = styles["copyToClipboardButton"],
                onClick = ref.current?.copyToClipboardOnClick() ?: {},
                attrs = { tabIndex = -1 }
            )
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
    add(PlayerRoster(label = "Unpaired players", players = players, partyId = party.id))

private fun notPairedPlayers(players: List<Player>, pairAssignments: PairAssignmentDocument?) =
    if (pairAssignments == null) {
        players
    } else {
        val currentlyPairedPlayerIds = pairAssignments.currentlyPairedPlayerIds()
        players.filterNot { player -> currentlyPairedPlayerIds.contains(player.id) }
    }

private fun PairAssignmentDocument.currentlyPairedPlayerIds() = pairs.flatMap { it.players }.map { it.player.id }

data class PrepareToSpinButton(val party: Party) : DataPropsBind<PrepareToSpinButton>(prepareToSpinButton)

private val prepareToSpinButton = tmFC<PrepareToSpinButton> { (party) ->
    Link {
        to = "/${party.id.value}/prepare/"
        tabIndex = -1
        draggable = false
        add(
            CouplingButton(
                sizeRuleSet = supersize,
                colorRuleSet = pink,
                className = styles["newPairsButton"],
                css = {
                    animationName = ident("pulsate")
                    animationDuration = 2.s
                    animationIterationCount = AnimationIterationCount.infinite
                    hover {
                        animationDuration = 0.75.s
                    }
                }
            )
        ) {
            +"Prepare to spin!"
        }
    }
}
