import kotlin.js.Json
import kotlin.js.json

fun Player.toJson() = json(
        "_id" to _id,
        "name" to name,
        "tribe" to tribe,
        "pins" to pins,
        "badge" to badge
)

fun Json.toPlayer(): Player = Player(
        _id = this["_id"]?.toString(),
        badge = this["badge"]?.toString(),
        name = this["name"]?.toString(),
        tribe = this["tribe"]?.toString(),
        pins = this["pins"],
        email = this["email"]?.toString(),
        callSignAdjective = this["callSignAdjective"]?.toString(),
        callSignNoun = this["callSignNoun"]?.toString(),
        imageURL = this["imageURL"]?.toString()
)
