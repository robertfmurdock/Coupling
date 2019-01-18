import PairingRule.PreferDifferentBadge

interface PlayerCandidatesFinder {

    fun findCandidates(players: Array<Player>, rule: PairingRule, player: Player) = players
            .filterNot { it == player }
            .filterByRule(rule, player.badge)
            .toTypedArray()

    private fun List<Player>.filterByRule(rule: PairingRule, badge: String?) = when (rule) {
        PreferDifferentBadge -> filter { otherPlayer -> otherPlayer.badge !== badge }
        else -> this
    }

}
