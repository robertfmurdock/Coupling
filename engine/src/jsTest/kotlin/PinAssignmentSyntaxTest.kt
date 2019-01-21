import kotlin.test.Test

class PinAssignmentSyntaxTest {

    @Test
    fun willDoTheObviousThingWhenThereIsOnlyOnePinAndOnePlayer() = setup(object : PinAssignmentSyntax {
        val pins = listOf(Pin(name = "Lucky"))
        val pete = Player(name = "Pete")
        val players = listOf(pete)
    }) exercise {
        players.assign(pins)
    } verify { result: List<Player> ->
        result.assertIsEqualTo(listOf(pete.copy(pins = pins)))
    }

    @Test
    fun willAssignNoPinsWhenThereAreNoPlayers() = setup(object : PinAssignmentSyntax {
        val pins = listOf(Pin(name = "Lucky"))
        val players = emptyList<Player>()
    }) exercise {
        players.assign(pins)
    } verify { result ->
        result.assertIsEqualTo(emptyList())
    }
}

