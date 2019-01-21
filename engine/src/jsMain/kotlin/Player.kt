data class Player(
        val _id: String? = null,
        val badge: String? = null,
        val name: String? = null,
        val tribe: String? = null,
        val pins: List<Pin>? = null,
        val email: String? = null,
        val callSignAdjective: String? = null,
        val callSignNoun: String? = null,
        val imageURL: String? = null
)

data class Pin(val _id: String? = null, val name: String? = null, val tribe: String? = null)