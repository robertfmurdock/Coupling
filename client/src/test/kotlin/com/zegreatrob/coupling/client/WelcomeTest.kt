package com.zegreatrob.coupling.client

import com.zegreatrob.coupling.client.external.react.get
import com.zegreatrob.coupling.client.external.react.useStyles
import com.zegreatrob.coupling.client.player.PlayerCardProps
import com.zegreatrob.coupling.client.welcome.RandomProvider
import com.zegreatrob.coupling.client.welcome.Welcome
import com.zegreatrob.coupling.client.welcome.WelcomeProps
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.minenzyme.ShallowWrapper
import com.zegreatrob.minenzyme.shallow
import com.zegreatrob.testmints.invoke
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
        shallow(Welcome, WelcomeProps(StubDispatchFunc(), randomProvider))
    } verify { wrapper ->
        wrapper.leftCard().props().player
            .assertIsEqualTo(
                Player(id = "Frodo", name = "Frodo", imageURL = imagePath("players/frodo"))
            )
        wrapper.rightCard().props().player
            .assertIsEqualTo(
                Player(id = "Sam", name = "Sam", imageURL = imagePath("players/samwise"))
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
        shallow(Welcome, WelcomeProps(StubDispatchFunc(), randomProvider))
    } verify { wrapper ->
        wrapper.leftCard().props().player
            .assertIsEqualTo(
                Player(id = "Batman", name = "Batman", imageURL = imagePath("players/grayson"))
            )
        wrapper.rightCard().props().player
            .assertIsEqualTo(
                Player(id = "Robin", name = "Robin", imageURL = imagePath("players/wayne"))
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
        shallow(Welcome, WelcomeProps(StubDispatchFunc(), randomProvider))
    } verify { wrapper ->
        wrapper.leftCard().props().player
            .assertIsEqualTo(
                Player(id = "Rosie", name = "Rosie", imageURL = imagePath("players/rosie"))
            )
        wrapper.rightCard().props().player
            .assertIsEqualTo(
                Player(id = "Wendy", name = "Wendy", imageURL = imagePath("players/wendy"))
            )
        wrapper.welcomeProverb().text()
            .assertIsEqualTo("Team up. Get things done.")
    }

    private fun ShallowWrapper<dynamic>.welcomeProverb() = find<Any>(".${styles["welcomeProverb"]}")

    private fun ShallowWrapper<dynamic>.leftCard() = find<PlayerCardProps>(".${styles["playerCard"]}.left")

    private fun ShallowWrapper<dynamic>.rightCard() = find<PlayerCardProps>(".${styles["playerCard"]}.right")

}
