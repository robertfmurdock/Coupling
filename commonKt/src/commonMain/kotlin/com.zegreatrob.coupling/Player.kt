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

infix fun Player.with(tribeId: TribeId) = TribeIdPlayer(tribeId, this)

data class TribeIdPlayer(val tribeId: TribeId, val player: Player)
