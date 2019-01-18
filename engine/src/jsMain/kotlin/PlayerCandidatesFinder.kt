import PairingRule.PreferDifferentBadge

interface PlayerCandidatesFinder {

    @JsName("findCandidates")
    fun findCandidates(players: Array<Player>, rule: PairingRule, player: Player) = players
            .filterNot { it == player }
            .filterByRule(rule, player.badge)
            .toTypedArray()

    private fun List<Player>.filterByRule(rule: PairingRule, badge: String?) = when (rule) {
        PreferDifferentBadge -> filter { otherPlayer -> otherPlayer.badge !== badge }
        else -> this
    }

}

external interface Player {
    val _id: String?
    val name: String?
    val badge: String?
    val pins: Any?
    val tribe: String?
    val email: String?
    val callSignAdjective: String?
    val callSignNoun: String?
    val imageURL: String?
}


