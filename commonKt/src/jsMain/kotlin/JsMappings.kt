import kotlin.js.*

fun Player.toJson(): Json = emptyArray<Pair<String, Any?>>()
        .plusIfNotNull("_id", _id)
        .plusIfNotNull("name", name)
        .plusIfNotNull("tribe", tribe)
        .plusIfNotNull("email", email)
        .plusIfNotNull("pins", pins?.toJson())
        .plusIfNotNull("badge", badge)
        .plusIfNotNull("callSignAdjective", callSignAdjective)
        .plusIfNotNull("callSignNoun", callSignNoun)
        .plusIfNotNull("imageURL", imageURL)
        .pairsToJson()

private fun Array<Pair<String, Any?>>.pairsToJson() = json(*this)

private fun Array<Pair<String, Any?>>.plusIfNotNull(key: String, value: Any?): Array<Pair<String, Any?>> {
    return if (value != null)
        plus(Pair(key, value))
    else
        this
}

private fun List<Pin>.toJson(): Array<Json> = map { it.toJson() }
        .toTypedArray()

private fun Pin.toJson() = json("_id" to _id, "tribe" to tribe, "name" to name)

@Suppress("UNCHECKED_CAST")
fun Json.toPlayer(): Player = Player(
        _id = stringValue("_id"),
        badge = this["badge"]?.unsafeCast<Int>(),
        name = stringValue("name"),
        tribe = stringValue("tribe"),
        email = stringValue("email"),
        callSignAdjective = stringValue("callSignAdjective"),
        callSignNoun = stringValue("callSignNoun"),
        imageURL = stringValue("imageURL"),
        pins = (this["pins"] as? Array<Json>)?.toPins() ?: emptyList()
)

fun Json.toTribe(): KtTribe = KtTribe(
        id = stringValue("id")!!,
        pairingRule = PairingRule.fromValue(this["pairingRule"] as? Int)
)

private fun Json.stringValue(key: String) = this[key]?.toString()

fun Array<Json>.toPins() = map {
    Pin(
            _id = it["_id"]?.toString(),
            name = it["name"]?.toString(),
            tribe = it["tribe"]?.toString()
    )
}

external interface PairingDocument {
    val pairs: Array<Array<Json>>?
    val date: Date
    val tribeId: String
}

@Suppress("unused")
@JsName("historyFromArray")
fun historyFromArray(history: Array<Json>) =
        history.map {
            it.toPairAssignmentDocument()
        }

fun Json.toPairAssignmentDocument() = PairAssignmentDocument(
        date = this["date"].let { if (it is String) Date(it) else it.unsafeCast<Date>() },
        pairs = this["pairs"].unsafeCast<Array<Array<Json>>?>()?.map(::pairFromArray) ?: listOf(),
        tribeId = this["tribeId"].unsafeCast<String>()
)

@JsName("pairFromArray")
fun pairFromArray(array: Array<Json>) = array.map { it.toPlayer() }.toPairs()

private fun List<Player>.toPairs() = when (size) {
    1 -> CouplingPair.Single(this[0])
    2 -> CouplingPair.Double(this[0], this[1])
    else -> CouplingPair.Empty
}

fun StatisticsReport.toJson() = json(
        "spinsUntilFullRotation" to spinsUntilFullRotation,
        "pairReports" to pairReports.map { it.toJson() }.toTypedArray(),
        "medianSpinDuration" to medianSpinDuration?.millisecondsInt
)

fun PairReport.toJson() = json(
        "pair" to pair.asArray().map { it.toJson() }.toTypedArray(),
        "timeSinceLastPaired" to when (timeSinceLastPair) {
            is TimeResultValue -> timeSinceLastPair.time
            NeverPaired -> "NeverPaired"
        }
)
