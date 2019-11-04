package com.zegreatrob.coupling.sdk

import com.zegreatrob.coupling.model.player.Player
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

class PlayersTest {

    companion object {
        private val configPort = process.env.PORT
        private val host = "http://localhost:${configPort}"
        private const val userEmail = "test@test.tes"


        private suspend fun setup(): Axios {
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
    }

    class GET {
        @Test
        fun isNotAllowedForUsersWithoutAccess() = testAsync {
            val hostAxios = setup()
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

        private inline fun catchError(function: () -> List<Player>) = try {
            function()
            json()
        } catch (error: dynamic) {
            error.response.unsafeCast<Json>()
        }

    }


}