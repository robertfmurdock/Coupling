class GameExamplesTest {

    class WithUniformBadgesAndLongestTimeRule {

        companion object {
            val tribe = KtTribe(id = "JLA", pairingRule = PairingRule.LongestTime)

            val bruce = Player(id = "1", name = "Batman", tribe = tribe.id, badge = 0)
            val hal = Player(id = "2", name = "Green Lantern", tribe = tribe.id, badge = 0)
            val barry = Player(id = "3", name = "Flash", tribe = tribe.id, badge = 0)
            val john = Player(id = "4", name = "Martian Manhunter", tribe = tribe.id, badge = 0)
            val clark = Player(id = "5", name = "Superman", tribe = tribe.id, badge = 0)
            val diana = Player(id = "6", name = "Wonder Woman", tribe = tribe.id, badge = 0)

            val players = listOf(
                    bruce,
                    hal,
                    barry,
                    john,
                    clark,
                    diana
            )
        }

    }

}