data class Player(
        val _id: String? = null,
        val badge: Int? = null,
        val name: String? = null,
        val tribe: String? = null,
        val pins: List<Pin>? = emptyList(),
        val email: String? = null,
        val callSignAdjective: String? = null,
        val callSignNoun: String? = null,
        val imageURL: String? = null
)
