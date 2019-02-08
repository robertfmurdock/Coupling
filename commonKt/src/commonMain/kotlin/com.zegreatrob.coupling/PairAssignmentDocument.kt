import com.soywiz.klock.DateTime

data class PairAssignmentDocument(
        val date: DateTime,
        val pairs: List<PinnedCouplingPair>,
        val tribeId: TribeId,
        val id: PairAssignmentDocumentId? = null
)