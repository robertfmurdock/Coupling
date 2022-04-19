package com.zegreatrob.coupling.client

import com.zegreatrob.coupling.client.external.react.get
import com.zegreatrob.coupling.client.external.react.useStyles
import com.zegreatrob.coupling.client.player.playerCard
import com.zegreatrob.coupling.client.welcome.RandomProvider
import com.zegreatrob.coupling.client.welcome.Welcome
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.minenzyme.ShallowWrapper
import com.zegreatrob.minenzyme.dataprops
import com.zegreatrob.minenzyme.shallow
import com.zegreatrob.testmints.setup
import kotlin.test.Test

class WelcomeTest {

    private val styles = useStyles("Welcome")

    @Test
    fun whenZeroIsRolledWillShowHobbits() = setup(object {
        val randomProvider = object : RandomProvider {
            override fun nextRandomInt(until: Int) = 0
        }
    }) exercise {
        shallow(Welcome(randomProvider))
    } verify { wrapper ->
        wrapper.leftCard()?.player
            .assertIsEqualTo(
                Player(id = "Frodo", name = "Frodo", imageURL = pngPath("players/frodo"))
            )
        wrapper.rightCard()?.player
            .assertIsEqualTo(
                Player(id = "Sam", name = "Sam", imageURL = pngPath("players/samwise"))
            )
        wrapper.welcomeProverb().text()
            .assertIsEqualTo("Together, climb mountains.")
    }

    @Test
    fun whenOneIsRolledWillShowTheDynamicDuo() = setup(object {
        val randomProvider = object : RandomProvider {
            override fun nextRandomInt(until: Int) = 1
        }
    }) exercise {
        shallow(Welcome(randomProvider))
    } verify { wrapper ->
        wrapper.leftCard()?.player
            .assertIsEqualTo(
                Player(id = "Batman", name = "Batman", imageURL = pngPath("players/grayson"))
            )
        wrapper.rightCard()?.player
            .assertIsEqualTo(
                Player(id = "Robin", name = "Robin", imageURL = pngPath("players/wayne"))
            )
        wrapper.welcomeProverb().text()
            .assertIsEqualTo("Clean up the city, together.")
    }

    @Test
    fun whenTwoIsRolledWillShowTheHeroesOfWWII() = setup(object {
        val randomProvider = object : RandomProvider {
            override fun nextRandomInt(until: Int) = 2
        }
    }) exercise {
        shallow(Welcome(randomProvider))
    } verify { wrapper ->
        wrapper.leftCard()?.player
            .assertIsEqualTo(
                Player(id = "Rosie", name = "Rosie", imageURL = pngPath("players/rosie"))
            )
        wrapper.rightCard()?.player
            .assertIsEqualTo(
                Player(id = "Wendy", name = "Wendy", imageURL = pngPath("players/wendy"))
            )
        wrapper.welcomeProverb().text()
            .assertIsEqualTo("Team up. Get things done.")
    }

    private fun ShallowWrapper<dynamic>.welcomeProverb() = find<Any>(".${styles["welcomeProverb"]}")

    private fun ShallowWrapper<dynamic>.leftCard() = find(playerCard).map { it.dataprops() }
        .find { it.className?.toString()?.contains("left") ?: false }

    private fun ShallowWrapper<dynamic>.rightCard() = find(playerCard).map { it.dataprops() }
        .find { it.className?.toString()?.contains("right") ?: false }
}
