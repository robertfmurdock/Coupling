import com.soywiz.klock.DateTime

data class PairAssignmentDocument(val date: DateTime, val pairs: List<CouplingPair>, val tribeId: String)