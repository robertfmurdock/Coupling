import com.soywiz.klock.DateTime

data class RunGameAction(
        val players: List<Player>,
        val pins: List<Pin>,
        val history: List<PairAssignmentDocument>,
        val tribe: KtTribe
)

interface RunGameActionDispatcher : Clock, PinAssignmentSyntax {

    val actionDispatcher: FindNewPairsActionDispatcher

    private fun FindNewPairsAction.performThis() = with(actionDispatcher) { perform() }

    fun RunGameAction.perform() = addPinsToPlayers()
            .let { pinnedPlayers -> findNewPairs(pinnedPlayers) }
            .let { pairAssignments -> pairAssignmentDocument(pairAssignments, tribe.id) }

    private fun RunGameAction.findNewPairs(pinnedPlayers: List<Player>) = findNewPairsAction(pinnedPlayers)
            .performThis()

    private fun RunGameAction.findNewPairsAction(pinnedPlayers: List<Player>) = FindNewPairsAction(Game(
            history,
            pinnedPlayers,
            tribe.pairingRule
    ))

    private fun RunGameAction.addPinsToPlayers() = players.assign(pins)

    private fun pairAssignmentDocument(pairAssignments: List<CouplingPair>, tribeId: String) =
            PairAssignmentDocument(
                    currentDate(),
                    pairAssignments,
                    tribeId
            )
}

interface Clock {
    fun currentDate() = DateTime.now()
}

