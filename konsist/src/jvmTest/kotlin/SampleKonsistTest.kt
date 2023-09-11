import com.lemonappdev.konsist.api.Konsist
import com.lemonappdev.konsist.api.ext.list.withNameEndingWith
import com.zegreatrob.minassert.assertIsEqualTo
import kotlin.test.Test

class SampleKonsistTest {

    @Test
    fun modelWillNotIncludeCommandsOrQueries() = Konsist
        .scopeFromModule(moduleName = "libraries/model")
        .classes()
        .withNameEndingWith("Command", "Query")
        .assertIsEqualTo(emptyList())
}
