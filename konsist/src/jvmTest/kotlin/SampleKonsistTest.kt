import com.lemonappdev.konsist.api.Konsist
import com.lemonappdev.konsist.api.ext.list.withNameEndingWith
import com.lemonappdev.konsist.api.verify.assert
import com.zegreatrob.minassert.assertIsEqualTo
import kotlin.test.Test

class SampleKonsistTest {

    @Test
    fun modelWillNotIncludeCommandsOrQueries() = Konsist
        .scopeFromModule(moduleName = "libraries/model")
        .classes()
        .withNameEndingWith("Command", "Query")
        .assertIsEqualTo(emptyList())

    @Test
    fun actionWillModelCommandsAndQueriesCorrectly() = Konsist
        .scopeFromModule("libraries/action", "server/action")
        .classes()
        .withNameEndingWith("Command", "Query")
        .run {
            assert { it.isTopLevel }
            assert { it.hasAnnotations("ActionMint") }
            assert { it.containsInterface { dispatcher -> dispatcher.name == "Dispatcher" } }
        }
}
