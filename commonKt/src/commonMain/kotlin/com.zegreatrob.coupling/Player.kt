data class Player(
        val id: String? = null,
        val badge: Int? = null,
        val name: String? = null,
        val email: String? = null,
        val callSignAdjective: String? = null,
        val callSignNoun: String? = null,
        val imageURL: String? = null
)

inline class TribeId(val value: String)

data class TribeIdPlayer(val player: Player, val tribeId: TribeId)
