import com.soywiz.klock.DateTime

data class PairAssignmentDocument(
        val date: DateTime,
        val pairs: List<PinnedCouplingPair>,
        val id: PairAssignmentDocumentId? = null
)

fun PairAssignmentDocument.with(tribeId: TribeId) = TribeIdPairAssignmentDocument(tribeId, this)

data class TribeIdPairAssignmentDocument(val tribeId: TribeId, val document: PairAssignmentDocument)