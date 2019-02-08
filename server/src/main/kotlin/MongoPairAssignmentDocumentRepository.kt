import com.soywiz.klock.internal.toDate
import com.soywiz.klock.internal.toDateTime
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.await
import kotlin.js.*

interface MongoPairAssignmentDocumentRepository : PairAssignmentDocumentRepository,
        PlayerToDbSyntax,
        DbRecordInfoSyntax,
        DbRecordLoadSyntax,
        DbRecordDeleteSyntax {

    val jsRepository: dynamic

    override suspend fun save(tribeIdPairAssignmentDocument: TribeIdPairAssignmentDocument) = tribeIdPairAssignmentDocument
            .toDbJson()
            .let {
                jsRepository.historyCollection.insert(it).unsafeCast<Promise<Unit>>().await()
            }

    override suspend fun delete(pairAssignmentDocumentId: PairAssignmentDocumentId) {
        deleteEntity(
                id = pairAssignmentDocumentId.value,
                collection = jsRepository.historyCollection,
                entityName = "Pair Assignments",
                toDomain = { toPairAssignmentDocument() },
                toDbJson = { toDbJson() }
        )
    }

    override fun getPairAssignmentsAsync(tribeId: TribeId): Deferred<List<PairAssignmentDocument>> = GlobalScope.async {
        findByQuery(json("tribe" to tribeId.value), jsRepository.historyCollection)
                .map { json -> json.toPairAssignmentDocument().document }
                .sortedByDescending { it.date }
    }

    private fun TribeIdPairAssignmentDocument.toDbJson() = json(
            "id" to document.id?.value,
            "date" to document.date.toDate(),
            "pairs" to document.toDbJsPairs(),
            "tribe" to tribeId.value
    ).addRecordInfo()

    private fun PairAssignmentDocument.toDbJsPairs() = pairs.map {
        it.players
                .map { player -> player.toJson() }
                .toTypedArray()
    }
            .toTypedArray()

    private fun PinnedPlayer.toJson(): Json = player.toDbJson().apply { this["pins"] = pins.toDbJson() }

    private fun List<Pin>.toDbJson(): Array<Json> = map { it.toDbJson() }
            .toTypedArray()

    private fun Pin.toDbJson() = json("id" to _id, "tribe" to tribe, "name" to name)

    private fun historyFromArray(history: Array<Json>) = history.map {
        it.toPairAssignmentDocument()
    }

    private fun Json.toPairAssignmentDocument() = TribeIdPairAssignmentDocument(
            TribeId(this["tribe"].unsafeCast<String>()),
            PairAssignmentDocument(
                    date = this["date"].let { if (it is String) Date(it) else it.unsafeCast<Date>() }.toDateTime(),
                    pairs = this["pairs"].unsafeCast<Array<Array<Json>>?>()?.map(::pairFromArray) ?: listOf(),
                    id = idStringValue()
                            .let(::PairAssignmentDocumentId)
            )
    )

    private fun Json.idStringValue() = let { this["id"].unsafeCast<Json?>() ?: this["_id"] }.toString()

    @JsName("pairFromArray")
    fun pairFromArray(array: Array<Json>) = array.map {
        PinnedPlayer(it.fromDbToPlayer(), it["pins"].unsafeCast<Array<Json>?>()?.toPins() ?: emptyList())
    }.toPairs()

    private fun Array<Json>.toPins() = map {
        Pin(
                _id = it["id"]?.toString() ?: it["_id"]?.toString(),
                name = it["name"]?.toString(),
                tribe = it["tribe"]?.toString()
        )
    }

    private fun List<PinnedPlayer>.toPairs() = PinnedCouplingPair(this)
}
