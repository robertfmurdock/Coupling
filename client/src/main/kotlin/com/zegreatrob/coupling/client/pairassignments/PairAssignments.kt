package com.zegreatrob.coupling.client.pairassignments

import com.zegreatrob.coupling.client.*
import com.zegreatrob.coupling.client.external.reactdnd.DndProvider
import com.zegreatrob.coupling.client.external.reactdndhtml5backend.HTML5Backend
import com.zegreatrob.coupling.client.player.PlayerCardProps
import com.zegreatrob.coupling.client.player.PlayerRosterProps
import com.zegreatrob.coupling.client.player.playerCard
import com.zegreatrob.coupling.client.player.playerRoster
import com.zegreatrob.coupling.client.tribe.TribeBrowserProps
import com.zegreatrob.coupling.client.tribe.tribeBrowser
import com.zegreatrob.coupling.common.entity.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.common.entity.pairassignmentdocument.PinnedCouplingPair
import com.zegreatrob.coupling.common.entity.pairassignmentdocument.PinnedPlayer
import com.zegreatrob.coupling.common.entity.player.Player
import com.zegreatrob.coupling.common.entity.player.callsign.CallSign
import com.zegreatrob.coupling.common.entity.tribe.KtTribe
import com.zegreatrob.coupling.common.entity.tribe.TribeId
import com.zegreatrob.coupling.common.toJson
import kotlinx.coroutines.await
import kotlinx.coroutines.launch
import kotlinx.html.DIV
import kotlinx.html.id
import kotlinx.html.js.onClickFunction
import react.RBuilder
import react.RProps
import react.ReactElement
import react.dom.RDOMBuilder
import react.dom.a
import react.dom.div
import react.dom.span
import kotlin.browser.window
import kotlin.js.Promise

object PairAssignments : ComponentProvider<PairAssignmentsProps>(), PairAssignmentsBuilder

data class PairAssignmentsProps(
        val tribe: KtTribe,
        val players: List<Player>,
        val pairAssignments: PairAssignmentDocument?,
        val pathSetter: (String) -> Unit,
        val coupling: dynamic
) : RProps

external interface PairAssignmentsStyles {
    val className: String
}

const val dragItemType = "PLAYER"

typealias PairAssignmentRenderer = ScopedPropsStylesBuilder<PairAssignmentsProps, PairAssignmentsStyles>

interface PairAssignmentsBuilder : ScopedStyledComponentBuilder<PairAssignmentsProps, PairAssignmentsStyles> {

    override val componentPath: String get() = "pairassignments/PairAssignments"

    override fun build() = buildBy {
        val (pairAssignments, setPairAssignments) = useState(props.pairAssignments)

        val swapCallback = { droppedPlayerId: String, targetPlayer: PinnedPlayer, targetPair: PinnedCouplingPair ->
            setPairAssignments(pairAssignments?.swapPlayers(droppedPlayerId, targetPlayer, targetPair))
        }
        val tribe = props.tribe
        val players = props.players
        val pathSetter = props.pathSetter
        {
            DndProvider {
                attrs { backend = HTML5Backend }
                div(classes = styles.className) {
                    div {
                        tribeBrowser(TribeBrowserProps(tribe, pathSetter))
                        currentPairAssignments(pairAssignments, swapCallback)()
                    }
                    playerRoster(PlayerRosterProps(
                            label = "Unpaired players",
                            players = players.filterNotPaired(pairAssignments),
                            tribeId = tribe.id,
                            pathSetter = pathSetter
                    ))
                    serverMessage(ServerMessageProps(
                            tribeId = tribe.id,
                            useSsl = "https:" == window.location.protocol
                    ))
                }
            }
        }
    }

    private fun List<Player>.filterNotPaired(pairAssignments: PairAssignmentDocument?) =
            if (pairAssignments == null) {
                this
            } else {
                val currentlyPairedPlayerIds = pairAssignments.currentlyPairedPlayerIds()
                filterNot { player -> currentlyPairedPlayerIds.contains(player.id) }
            }

    private fun PairAssignmentDocument.currentlyPairedPlayerIds() = pairs.flatMap { it.players }.map { it.player.id }

    private fun PairAssignmentRenderer.currentPairAssignments(
            pairAssignments: PairAssignmentDocument?,
            swapCallback: (String, PinnedPlayer, PinnedCouplingPair) -> Unit
    ): RBuilder.() -> ReactElement {
        val tribe = props.tribe

        return {
            div(classes = "current pair-assignments") {
                pairAssignmentsHeader(pairAssignments)
                div {
                    attrs { id = "pair-assignments-content" }
                    pairAssignments?.pairs?.mapIndexed { index, pair ->
                        assignedPair(index, pair, props, swapCallback, pairAssignments)
                    }
                }
                div {
                    pairAssignments?.let {
                        if (it.id == null) {
                            saveButton(pairAssignments)()
                        }
                    }
                }

                a(href = "/${tribe.id.value}/prepare/", classes = "large pink button") {
                    attrs { id = "new-pairs-button" }
                    +"Prepare to spin!"
                }

                a(href = "/${tribe.id.value}/history/", classes = "large blue button") {
                    attrs { id = "view-history-button" }
                    +"View history!"
                }

                a(href = "/${tribe.id.value}/players/retired", classes = "large yellow button") {
                    attrs { id = "retired-players-button" }
                    +"View retirees!"
                }
            }
        }
    }

    private fun PairAssignmentRenderer.saveButton(pairAssignments: PairAssignmentDocument): RBuilder.() -> ReactElement {
        return {
            a(classes = "super green button") {
                attrs {
                    id = "save-button"
                    onClickFunction = {
                        scope.launch {
                            val tribeId = props.tribe.id
                            saveCurrentPairAssignments(pairAssignments, tribeId, props.coupling)
                            props.pathSetter("/${tribeId.value}/pairAssignments/current/")
                        }
                    }
                }
                +"Save!"
            }
        }
    }

    private suspend fun saveCurrentPairAssignments(pairAssignments: PairAssignmentDocument, tribeId: TribeId, coupling: dynamic) {
        coupling.saveCurrentPairAssignments(pairAssignments.toJson(), tribeId.value)
                .unsafeCast<Promise<Unit>>()
                .await()
    }

    private fun PairAssignmentDocument.swapPlayers(
            droppedPlayerId: String,
            targetPlayer: PinnedPlayer,
            targetPair: PinnedCouplingPair
    ): PairAssignmentDocument {
        val sourcePair = pairs.findPairContainingPlayer(droppedPlayerId)
        val droppedPlayer = sourcePair?.players?.firstOrNull { it.player.id == droppedPlayerId }

        if (sourcePair == targetPair || droppedPlayer == null) {
            return this
        }

        return copy(
                pairs = pairs.map { pair ->
                    when (pair) {
                        targetPair -> replacePlayer(pair, targetPlayer, droppedPlayer)
                        sourcePair -> replacePlayer(pair, droppedPlayer, targetPlayer)
                        else -> pair
                    }
                }
        )
    }

    private fun replacePlayer(
            pair: PinnedCouplingPair,
            playerToReplace: PinnedPlayer,
            replacement: PinnedPlayer
    ) = PinnedCouplingPair(pair.players.map { pinnedPlayer ->
        if (pinnedPlayer == playerToReplace) {
            replacement
        } else {
            pinnedPlayer
        }
    })

    private fun List<PinnedCouplingPair>.findPairContainingPlayer(droppedPlayerId: String) =
            firstOrNull { pair ->
                pair.players.any { player -> player.player.id == droppedPlayerId }
            }

    private fun RBuilder.assignedPair(
            index: Int,
            pair: PinnedCouplingPair,
            props: PairAssignmentsProps,
            swapCallback: (String, PinnedPlayer, PinnedCouplingPair) -> Unit,
            pairAssignmentDocument: PairAssignmentDocument?
    ) {

        val (tribe) = props
        val callSign = findCallSign(pair)

        span(classes = "pair") {
            attrs { key = "$index" }
            callSign(tribe, callSign)
            pair.players.map { pinnedPlayer ->
                if (pairAssignmentDocument != null && pairAssignmentDocument.id == null) {
                    draggablePlayer(DraggablePlayerProps(
                            pinnedPlayer,
                            tribe,
                            pairAssignmentDocument
                    ) { droppedPlayerId -> swapCallback(droppedPlayerId, pinnedPlayer, pair) })
                } else {
                    val player = pinnedPlayer.player
                    playerCard(PlayerCardProps(
                            tribe.id,
                            player,
                            props.pathSetter,
                            false
                    ), key = player.id)
                }
            }
        }
    }

    private fun RBuilder.callSign(tribe: KtTribe, callSign: CallSign?) = div {
        if (tribe.callSignsEnabled && callSign != null) {
            span(classes = "call-sign") {
                +"${callSign.adjective} ${callSign.noun}"
            }
        }
    }

    private fun findCallSign(pair: PinnedCouplingPair): CallSign? {
        val nounPlayer = pair.toPair().asArray().getOrNull(0)
        val adjectivePlayer = pair.toPair().asArray().getOrNull(1) ?: nounPlayer

        val adjective = adjectivePlayer?.callSignAdjective
        val noun = nounPlayer?.callSignNoun
        return if (adjective != null && noun != null) {
            CallSign(adjective, noun)
        } else {
            null
        }

    }

    private fun RDOMBuilder<DIV>.pairAssignmentsHeader(pairAssignments: PairAssignmentDocument?) {
        if (pairAssignments != null) {
            div {
                div {
                    div(classes = "pair-assignments-header") {
                        +"Couples for ${pairAssignments.dateText()}"
                    }
                }
            }
        } else {
            div(classes = "no-pairs-notice") {
                +"No pair assignments yet!"
            }
        }
    }

}
