package com.zegreatrob.coupling.client.components.pin

import com.zegreatrob.coupling.stubmodel.stubPin
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.setup
import com.zegreatrob.wrapper.testinglibrary.react.TestingLibraryReact
import kotlinx.dom.hasClass
import org.w3c.dom.Element
import kotlin.test.Test

class PinButtonTest {

    @Test
    fun whenGivenPinWithSimpleIconWillUseStandardFontAwesomeTag() = setup(object {
        val pin = stubPin().copy(icon = "angry")
    }) exercise {
        TestingLibraryReact.render { PinButton(pin, onClick = {}) }
    } verify { wrapper ->
        wrapper.baseElement.getElementsByTagName("i").item(0)
            .assertIconHasClasses("fa", "fa-angry")
    }

    @Test
    fun whenGivenPinWithAlreadyDecoratedIconWillUseStandardFontAwesomeTag() = setup(object {
        val pin = stubPin().copy(icon = "fa-angry")
    }) exercise {
        TestingLibraryReact.render { PinButton(pin, onClick = {}) }
    } verify { wrapper ->
        wrapper.baseElement.getElementsByTagName("i").item(0)
            .assertIconHasClasses("fa", "fa-angry")
    }

    @Test
    fun whenGivenPinWithFullyDecoratedIconWillUseStandardFontAwesomeTag() = setup(object {
        val pin = stubPin().copy(icon = "far fa-angry")
    }) exercise {
        TestingLibraryReact.render { PinButton(pin, onClick = {}) }
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
