import kotlin.js.json

fun Player.toJson() = json(
        "_id" to _id,
        "name" to name,
        "tribe" to tribe,
        "pins" to pins,
        "badge" to badge
)