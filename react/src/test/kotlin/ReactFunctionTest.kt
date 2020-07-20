import com.zegreatrob.coupling.enzyme.external.shallow
import com.zegreatrob.coupling.react.external.react.reactFunction
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.invoke
import com.zegreatrob.testmints.setup
import react.RProps
import react.dom.div
import react.dom.span
import kotlin.test.Test

class ReactFunctionTest {

    data class FunProps(val first: Int, val second: String) : RProps

    @Test
    fun canUseDataObjectAndDestructureDuringRender() = setup(object {
        val component = reactFunction<FunProps> { (first, second) ->
            div {
                span { +"$first" }
                span { +second }
            }
        }
        val expectedFirst = 3948
        val expectedSecond = "9922"
    }) exercise {
        shallow(component, FunProps(expectedFirst, expectedSecond))
    } verify { result ->
        result.find<dynamic>("span")
            .map { it.text() }
            .toList()
            .assertIsEqualTo(
                listOf("$expectedFirst", expectedSecond)
            )
    }

}