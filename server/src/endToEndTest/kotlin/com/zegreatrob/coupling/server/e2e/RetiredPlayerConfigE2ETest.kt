package com.zegreatrob.coupling.server.e2e

import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.tribe.Tribe
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.model.tribe.with
import com.zegreatrob.coupling.server.e2e.CouplingLogin.sdkProvider
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.async.setupAsync
import com.zegreatrob.testmints.async.testAsync
import kotlinx.coroutines.await
import kotlin.test.Test

class RetiredPlayerConfigE2ETest {

    @Test
    fun willShowThePlayerData() = testAsync {
        val sdk = sdkProvider.await()
        setupAsync(object {
            val tribe = Tribe(TribeId("${randomInt()}-RetiredPlayerConfigE2E"))
            val player = Player("${randomInt()}-RetiredPlayerConfigE2E", name = "${randomInt()}-RetiredPlayerConfigE2E")
        }) {
            sdk.save(tribe)
            sdk.save(tribe.id.with(player))
            sdk.deletePlayer(tribe.id, player.id!!)
            CouplingLogin.loginProvider
        } exerciseAsync {
            RetiredPlayerConfig.goTo(tribe.id, player.id)
        } verifyAsync {
            RetiredPlayerConfig.playerNameTextField.getAttribute("value").await()
                .assertIsEqualTo(player.name)
        }
    }

}
