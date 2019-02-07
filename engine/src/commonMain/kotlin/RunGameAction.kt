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

    fun RunGameAction.perform() = findNewPairs()
            .assign(pins)
            .let { pairAssignments -> pairAssignmentDocument(pairAssignments, tribe.id) }

    private fun RunGameAction.findNewPairs() = findNewPairsAction()
            .performThis()

    private fun RunGameAction.findNewPairsAction() = FindNewPairsAction(Game(
            history,
            players,
            tribe.pairingRule
    ))

    private fun pairAssignmentDocument(pairAssignments: List<PinnedCouplingPair>, tribeId: String) =
            PairAssignmentDocument(
                    currentDate(),
                    pairAssignments,
                    tribeId,
                    null
            )
}

interface Clock {
    fun currentDate() = DateTime.now()
}

