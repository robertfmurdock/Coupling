package com.zegreatrob.coupling.sdk

import com.benasher44.uuid.uuid4
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.player.TribeIdPlayer
import com.zegreatrob.coupling.model.tribe.KtTribe
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.async.setupAsync
import com.zegreatrob.testmints.async.testAsync
import kotlin.js.Json
import kotlin.js.json
import kotlin.test.Test

class PlayersTest {

    companion object {
        inline fun catchError(function: () -> Any) = try {
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
            val sdk = authorizedSdk()
            setupAsync(object {}) exerciseAsync {
                catchError {
                    sdk.getPlayersAsync(tribeId).await()
                }
            } verifyAsync { result ->
                result["status"].assertIsEqualTo(404)
            }
        }

        @Test
        fun postIsNotAllowed() = testAsync {
            val sdk = authorizedSdk()
            setupAsync(object : Sdk by sdk {
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
            val sdk = authorizedSdk()
            setupAsync(object {
            }) exerciseAsync {
                catchError {
                    sdk.deletePlayer(tribeId, "player id")
                }
            } verifyAsync { result ->
                result["status"].assertIsEqualTo(404)
            }
        }
    }

    @Test
    fun postPlayersThenGetWillReturnAllAvailablePlayersOnTeam() = testAsync {
        val sdk = authorizedSdk()
        setupAsync(object {
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
            sdk.save(tribe)
            playersToSave
                .map { TribeIdPlayer(tribe.id, it) }
                .forEach { sdk.save(it) }
        } exerciseAsync {
            sdk.getPlayersAsync(tribe.id)
                .await()
        } verifyAsync { result ->
            result.assertIsEqualTo(playersToSave)
        }
    }

    @Test
    fun deleteWillRemoveAGivenPlayer() = testAsync {
        val sdk = authorizedSdk()
        setupAsync(object {
            val tribe = KtTribe(id = TribeId(uuid4().toString()))
            val player = Player(
                id = "${monk.id()}",
                name = "Awesome-O"
            )
        }) {
            sdk.save(tribe)
            sdk.save(TribeIdPlayer(tribe.id, player))
        } exerciseAsync {
            sdk.deletePlayer(tribe.id, player.id!!)
            sdk.getPlayersAsync(tribe.id).await()
        } verifyAsync { result ->
            result.contains(player).assertIsEqualTo(false)
        }
    }

    @Test
    fun deleteWillReturnErrorWhenPlayerDoesNotExist() = testAsync {
        val sdk = authorizedSdk()
        setupAsync(object {
            val tribe = KtTribe(id = TribeId(uuid4().toString()))
        }) {
            sdk.save(tribe)
        } exerciseAsync {
            catchError {
                sdk.deletePlayer(tribe.id, monk.id().toString())
            }
        } verifyAsync { result ->
            result["status"].assertIsEqualTo(404)
            result["data"].unsafeCast<Json>()["message"]
                .assertIsEqualTo("Player could not be deleted because they do not exist.")
        }
    }
}