package com.zegreatrob.coupling.sdk

import com.benasher44.uuid.uuid4
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.player.TribeIdPlayer
import com.zegreatrob.coupling.model.tribe.KtTribe
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.sdk.external.axios.Axios
import com.zegreatrob.coupling.sdk.external.axios.axios
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.async.setupAsync
import com.zegreatrob.testmints.async.testAsync
import kotlinx.coroutines.await
import kotlin.js.Json
import kotlin.js.json
import kotlin.test.Test


external val process: dynamic

@JsModule("axios-cookiejar-support")
external val axiosCookiejarSupport: dynamic

@JsModule("tough-cookie")
external val toughCookie: dynamic

@Suppress("unused")
class PlayersTest {

    companion object {
        private val configPort = process.env.PORT
        private val host = "http://localhost:${configPort}"
        private const val userEmail = "test@test.tes"

        private suspend fun authorizedAxios(): Axios {
            axiosCookiejarSupport.default(axios.default)
            @Suppress("UNUSED_VARIABLE") val jarType = toughCookie.CookieJar
            val cookieJar = js("new jarType")
            val hostAxios = axios.create(
                json(
                    "baseURL" to host,
                    "jar" to cookieJar,
                    "withCredentials" to true
                )
            )

            hostAxios.get(
                "/test-login?username=$userEmail&password=pw",
                json("maxRedirects" to 0, "validateStatus" to { true })
            )
                .await()
                .status
                .assertIsEqualTo(302)

            return hostAxios
        }

        private inline fun catchError(function: () -> List<Player>) = try {
            function()
            json()
        } catch (error: dynamic) {
            error.response.unsafeCast<Json>()
        }

    }

    class GET {
        @Test
        fun isNotAllowedForUsersWithoutAccess() = testAsync {
            val hostAxios = authorizedAxios()
            setupAsync(object : SdkPlayerGetter {
                override val axios: Axios get() = hostAxios
            }) exerciseAsync {
                catchError {
                    getPlayersAsync(TribeId("somebodyElsesTribe"))
                        .await()
                }
            } verifyAsync { result ->
                result["status"].assertIsEqualTo(404)
            }
        }

        @Test
        fun willReturnAllAvailablePlayersOnTeam() = testAsync {
            val hostAxios = authorizedAxios()
            setupAsync(object : SdkPlayerGetter, SdkPlayerSaver, SdkTribeSave {
                override val axios: Axios get() = hostAxios
                val tribe = KtTribe(
                    id = TribeId("et-${uuid4()}")
                )

                val playersToSave = listOf(
                    Player(
                        id = "${uuid4()}",
                        name = "Awesome-O",
                        callSignAdjective = "Awesome",
                        callSignNoun = "Sauce"
                    ),
                    Player(
                        id = "${uuid4()}",
                        name = "Awesome-O-2",
                        callSignAdjective = "Very",
                        callSignNoun = "Ok"
                    )
                )
            }) {
                save(tribe)
                playersToSave
                    .map { TribeIdPlayer(tribe.id, it) }
                    .forEach { save(it) }
            } exerciseAsync {
                getPlayersAsync(tribe.id)
                    .await()
            } verifyAsync { result ->
                result.assertIsEqualTo(playersToSave)
            }
        }
    }


}