package com.zegreatrob.coupling.client.components.spin

import com.zegreatrob.coupling.action.SpinCommand
import com.zegreatrob.coupling.action.fire
import com.zegreatrob.coupling.client.components.DispatchFunc
import com.zegreatrob.coupling.client.components.Paths.newPairAssignmentsPath
import com.zegreatrob.coupling.model.flatMap
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.model.pairassignmentdocument.PinnedCouplingPair
import com.zegreatrob.coupling.model.pairassignmentdocument.players
import com.zegreatrob.coupling.model.party.PartyDetails
import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.minreact.ReactFunc
import com.zegreatrob.minreact.nfc
import kotools.types.collection.toNotEmptyList
import kotools.types.text.NotBlankString
import react.Props
import react.router.useNavigate
import react.useState

external interface PrepareSpinProps : Props {
    var party: PartyDetails
    var players: List<Player>
    var currentPairsDoc: PairAssignmentDocument?
    var pins: List<Pin>
    var dispatchFunc: DispatchFunc<SpinCommand.Dispatcher>
}

@ReactFunc
val PrepareSpin by nfc<PrepareSpinProps> { (party, players, currentPairsDoc, pins, dispatchFunc) ->
    var playerSelections by useState(defaultSelections(players, currentPairsDoc))
    val (pinSelections, setPinSelections) = useState(pins.map(Pin::id).map(NotBlankString::toString))
    val navigate = useNavigate()
    val selectedPlayerIds = playerSelections.selectedPlayerIds().toNotEmptyList().getOrNull()
    PrepareSpinContent(
        party = party,
        playerSelections = playerSelections,
        pins = pins,
        pinSelections = pinSelections,
        setPlayerSelections = { playerSelections = it },
        selectPin = { pinId: String, selected: Boolean ->
            setPinSelections { if (selected) it + pinId else it - pinId }
        },
        onSpin = if (selectedPlayerIds == null) {
            null
        } else {
            dispatchFunc {
                fire(SpinCommand(party.id, selectedPlayerIds, pinSelections))
                navigate(party.newPairAssignmentsPath())
            }
        },
    )
}

private fun defaultSelections(players: List<Player>, currentPairsDoc: PairAssignmentDocument?) = players.map { player ->
    player to isInLastSetOfPairs(player, currentPairsDoc)
}

private fun isInLastSetOfPairs(player: Player, currentPairsDoc: PairAssignmentDocument?) = currentPairsDoc
    ?.pairs
    ?.flatMap(PinnedCouplingPair::players)
    ?.map(Player::id)
    ?.contains(player.id) == true

private fun List<Pair<Player, Boolean>>.selectedPlayerIds() = filter { (_, isSelected) -> isSelected }
    .map { it.first.id }
