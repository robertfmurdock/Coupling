package com.zegreatrob.coupling.sdk

import com.benasher44.uuid.uuid4
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.player.TribeIdPlayer
import com.zegreatrob.coupling.model.tribe.KtTribe
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.sdk.external.axios.Axios
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.async.setupAsync
import com.zegreatrob.testmints.async.testAsync
import kotlin.js.Json
import kotlin.js.json
import kotlin.test.Test

class PlayersTest {

    companion object {
        private inline fun catchError(function: () -> Any) = try {
            function()
            json()
        } catch (error: dynamic) {
            error.response.unsafeCast<Json>()
        }
    }

    class GivenUsersWithoutAccess {
        companion object {
            val tribeId = TribeId("somebodyElsesTribe")
        }

        @Test
        fun getIsNotAllowed() = testAsync {
            val hostAxios = authorizedAxios()
            setupAsync(object : SdkPlayerGetter {
                override val axios: Axios get() = hostAxios
            }) exerciseAsync {
                catchError {
                    getPlayersAsync(tribeId).await()
                }
            } verifyAsync { result ->
                result["status"].assertIsEqualTo(404)
            }
        }

        @Test
        fun postIsNotAllowed() = testAsync {
            val hostAxios = authorizedAxios()
            setupAsync(object : SdkPlayerSaver {
                override val axios: Axios get() = hostAxios
                val player = Player(
                    id = "${uuid4()}",
                    name = "Awesome-O",
                    callSignAdjective = "Awesome",
                    callSignNoun = "Sauce"
                )
            }) exerciseAsync {
                catchError {
                    save(TribeIdPlayer(tribeId, player))
                }
            } verifyAsync { result ->
                result["status"].assertIsEqualTo(404)
            }
        }

        @Test
        fun deleteIsNotAllowed() = testAsync {
            val hostAxios = authorizedAxios()
            setupAsync(object : SdkPlayerDeleter {
                override val axios: Axios get() = hostAxios
            }) exerciseAsync {
                catchError {
                    deletePlayer(tribeId, "player id")
                }
            } verifyAsync { result ->
                result["status"].assertIsEqualTo(404)
            }
        }
    }

    @Test
    fun postPlayersThenGetWillReturnAllAvailablePlayersOnTeam() = testAsync {
        val hostAxios = authorizedAxios()
        setupAsync(object : SdkPlayerGetter, SdkPlayerSaver, SdkTribeSave {
            override val axios: Axios get() = hostAxios
            val tribe = KtTribe(id = TribeId("et-${uuid4()}"))

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

    @Test
    fun deleteWillRemoveAGivenPlayer() = testAsync {
        val hostAxios = authorizedAxios()
        setupAsync(object : SdkPlayerGetter, SdkPlayerSaver, SdkTribeSave, SdkPlayerDeleter {
            override val axios get() = hostAxios
            val tribe = KtTribe(id = TribeId(uuid4().toString()))
            val player = Player(
                id = "${monk.id()}",
                name = "Awesome-O"
            )
        }) {
            save(tribe)
            save(TribeIdPlayer(tribe.id, player))
        } exerciseAsync {
            deletePlayer(tribe.id, player.id!!)
            getPlayersAsync(tribe.id).await()
        } verifyAsync { result ->
            result.contains(player).assertIsEqualTo(false)
        }
    }

    @Test
    fun deleteWillReturnErrorWhenPlayerDoesNotExist() = testAsync {
        val hostAxios = authorizedAxios()
        setupAsync(object : SdkTribeSave, SdkPlayerDeleter {
            override val axios get() = hostAxios
            val tribe = KtTribe(id = TribeId(uuid4().toString()))
        }) {
            save(tribe)
        } exerciseAsync {
            catchError {
                deletePlayer(tribe.id, monk.id().toString())
            }
        } verifyAsync { result ->
            result["status"].assertIsEqualTo(404)
            result["data"].unsafeCast<Json>()["message"]
                .assertIsEqualTo("Player could not be deleted because they do not exist.")
        }
    }


}