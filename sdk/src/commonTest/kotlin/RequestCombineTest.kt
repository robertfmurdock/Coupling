import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.sdk.BatchingPartyGQLPerformer
import com.zegreatrob.coupling.sdk.PartyGQLPerformer
import com.zegreatrob.coupling.sdk.QueryPerformer
import com.zegreatrob.coupling.sdk.Sdk
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.async.asyncSetup
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.putJsonArray
import kotlinx.serialization.json.putJsonObject
import kotlin.test.Test

class RequestCombineTest {

    @Test
    fun whenMultipleGetsAreCalledInCloseProximityWillOnlyMakeOneGraphQLCall() = asyncSetup(object {
        val performer = StubQueryPerformer1()
        val sdk = object : Sdk, PartyGQLPerformer by BatchingPartyGQLPerformer(performer) {
            override suspend fun getToken() = ""
        }
        val partyId = PartyId("Random")
    }) exercise {
        with(sdk) {
            coroutineScope {
                launch { partyId.getPlayerList() }
                launch { partyId.getPins() }
            }
        }
    } verify {
        performer.allPostCalls.size.assertIsEqualTo(1)
    }
}

class StubQueryPerformer1 : QueryPerformer {
    val allPostCalls = mutableListOf<JsonElement>()

    override suspend fun doQuery(body: String): JsonElement =
        stubResponseData().apply { allPostCalls.add(JsonPrimitive(body)) }

    override suspend fun doQuery(body: JsonElement): JsonElement =
        stubResponseData().apply { allPostCalls.add(body) }

    override fun postAsync(body: JsonElement): Deferred<JsonElement> =
        CompletableDeferred(stubResponseData().apply { allPostCalls.add(body) })

    override suspend fun get(path: String): JsonElement = JsonNull
}

private fun stubResponseData() = buildJsonObject {
    putJsonObject("data") {
        putJsonObject("data") {
            putJsonObject("party") {
                putJsonArray("playerList") {}
                putJsonArray("pinList") {}
            }
        }
    }
}
