package com.zegreatrob.coupling.server.e2e

import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.tribe.Tribe
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.model.tribe.with
import com.zegreatrob.minassert.assertIsEqualTo
import kotlinx.coroutines.await
import kotlin.test.Test

class RetiredPlayerConfigE2ETest {

    @Test
    fun willShowThePlayerData() = sdkSetup(object : SdkContext() {
        val tribe = Tribe(TribeId("${randomInt()}-RetiredPlayerConfigE2E"))
        val player = Player("${randomInt()}-RetiredPlayerConfigE2E", name = "${randomInt()}-RetiredPlayerConfigE2E")
    }) {
        sdk.save(tribe)
        sdk.save(tribe.id.with(player))
        sdk.deletePlayer(tribe.id, player.id!!)
        CouplingLogin.loginProvider.await()
    } exercise {
        RetiredPlayerConfig.goTo(tribe.id, player.id)
    } verify {
        RetiredPlayerConfig.playerNameTextField.getAttribute("value").await()
            .assertIsEqualTo(player.name)
    }

}
