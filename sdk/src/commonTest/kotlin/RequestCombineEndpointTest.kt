import com.benasher44.uuid.uuid4
import com.zegreatrob.coupling.action.pairassignmentdocument.RequestSpinAction
import com.zegreatrob.coupling.action.player.SavePlayerCommand
import com.zegreatrob.coupling.action.user.UserQuery
import com.zegreatrob.coupling.model.party.Party
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.model.party.with
import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.coupling.model.pin.pin
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.player.player
import com.zegreatrob.coupling.sdk.Sdk
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.async.asyncSetup
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlin.test.Test

class RequestCombineEndpointTest {
    @Test
    fun postPlayersAndPinsThenGet() = asyncSetup.with({
        val sdk = authorizedSdk()
        object : Sdk by sdk {
            val party = Party(id = PartyId("et-${uuid4()}"))
            val playersToSave = listOf(
                Player(
                    id = "${uuid4()}",
                    name = "Awesome-O",
                    callSignAdjective = "Awesome",
                    callSignNoun = "Sauce",
                    avatarType = null,
                ),
            )
            val pinsToSave = listOf(Pin(uuid4().toString(), "1"))
            override suspend fun perform(query: UserQuery) = sdk.perform(query)
            override suspend fun perform(action: RequestSpinAction) = sdk.perform(action)
            override suspend fun perform(command: SavePlayerCommand) = sdk.perform(command)
        }
    }) {
        party.save()
        party.id.with(pinsToSave)
            .forEach { it.save() }
        party.id.with(playersToSave)
            .forEach { it.save() }
    } exercise {
        coroutineScope {
            val a1 = async { playerRepository.getPlayers(party.id).map { it.data.player } }
            val a2 = async { pinRepository.getPins(party.id).map { it.data.pin } }
            a1.await() to a2.await()
        }
    } verify { (players, pins) ->
        players.assertIsEqualTo(playersToSave)
        pins.assertIsEqualTo(pinsToSave)
    }
}
