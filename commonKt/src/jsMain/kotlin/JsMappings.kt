import kotlin.js.Json
import kotlin.js.json

fun Player.toJson() = json(
        "_id" to _id,
        "name" to name,
        "tribe" to tribe,
        "pins" to pins?.toJson(),
        "badge" to badge
)

private fun List<Pin>.toJson(): Array<Json> = map { it.toJson() }
        .toTypedArray()

private fun Pin.toJson() = json("_id" to _id, "tribe" to tribe, "name" to name)

@Suppress("UNCHECKED_CAST")
fun Json.toPlayer(): Player = Player(
        _id = stringValue("_id"),
        badge = stringValue("badge"),
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
}

@Suppress("unused")
@JsName("historyFromArray")
fun historyFromArray(history: Array<PairingDocument>) =
        history.map { HistoryDocument(it.pairs?.map(::pairFromArray) ?: listOf()) }

@JsName("pairFromArray")
fun pairFromArray(array: Array<Json>) = array.map { it.toPlayer() }.toPairs()

private fun List<Player>.toPairs() = when (size) {
    1 -> CouplingPair.Single(this[0])
    2 -> CouplingPair.Double(this[0], this[1])
    else -> CouplingPair.Empty
}