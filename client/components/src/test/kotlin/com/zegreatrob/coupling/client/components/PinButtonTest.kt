package com.zegreatrob.coupling.client.components

import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.minreact.create
import com.zegreatrob.testmints.setup
import com.zegreatrob.wrapper.testinglibrary.react.TestingLibraryReact.render
import kotlinx.dom.hasClass
import org.w3c.dom.Element
import kotlin.test.Test

class PinButtonTest {

    @Test
    fun whenGivenPinWithSimpleIconWillUseStandardFontAwesomeTag() = setup(object {
        val pin = Pin(icon = "angry")
    }) exercise {
        render(PinButton(pin, onClick = {}).create())
    } verify { wrapper ->
        wrapper.baseElement.getElementsByTagName("i").item(0)
            .assertIconHasClasses("fa", "fa-angry")
    }

    @Test
    fun whenGivenPinWithAlreadyDecoratedIconWillUseStandardFontAwesomeTag() = setup(object {
        val pin = Pin(icon = "fa-angry")
    }) exercise {
        render(PinButton(pin, onClick = {}).create())
    } verify { wrapper ->
        wrapper.baseElement.getElementsByTagName("i").item(0)
            .assertIconHasClasses("fa", "fa-angry")
    }

    @Test
    fun whenGivenPinWithFullyDecoratedIconWillUseStandardFontAwesomeTag() = setup(object {
        val pin = Pin(icon = "far fa-angry")
    }) exercise {
        render(PinButton(pin, onClick = {}).create())
    } verify { wrapper ->
        wrapper.baseElement.getElementsByTagName("i").item(0)
            .assertIconHasClasses("far", "fa-angry")
            ?.hasClass("fa")
            .assertIsEqualTo(false, "should not have fa")
    }

    private fun Element?.assertIconHasClasses(prefixClass: String, iconClass: String): Element? {
        this?.hasClass(prefixClass)
            .assertIsEqualTo(true, "Did not have class $prefixClass")
        this?.hasClass(iconClass)
            .assertIsEqualTo(true, "Did not have class $iconClass")
        return this
    }
}
