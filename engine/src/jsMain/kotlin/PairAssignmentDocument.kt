import kotlin.js.Date

data class PairAssignmentDocument(val date: Date, val expectedPairingAssignments: List<CouplingPair>, val tribeId: String)