import com.lemonappdev.konsist.api.KoModifier
import com.lemonappdev.konsist.api.Konsist
import com.lemonappdev.konsist.api.declaration.KoClassDeclaration
import com.lemonappdev.konsist.api.declaration.KoInterfaceDeclaration
import com.lemonappdev.konsist.api.ext.list.withNameEndingWith
import com.lemonappdev.konsist.api.verify.assert
import com.zegreatrob.minassert.assertIsEqualTo
import kotlin.test.Test

class KonsistTest {

    @Test
    fun modelWillNotIncludeCommandsOrQueries() = Konsist
        .scopeFromModule(moduleName = "libraries/model")
        .classes()
        .withNameEndingWith("Command", "Query")
        .assertIsEqualTo(emptyList())

    @Test
    fun actionsIncludeDispatcherInterfaces() = Konsist
        .scopeFromModule("libraries/action", "server/action", "client", "server")
        .classes()
        .withNameEndingWith("Command", "Query")
        .filter { !it.hasModifier(KoModifier.EXTERNAL) }
        .run {
            assert { it.isTopLevel }
            assert { it.hasAnnotationWithName("ActionMint") }
            assert { it.containsInterface(predicate = ::isNamedDispatcher) }
            assert(function = ::dispatcherHasPerformFunction)
        }

    private fun dispatcherHasPerformFunction(classDeclaration: KoClassDeclaration) =
        classDeclaration.containsInterface {
            withPerformFunction(it, classDeclaration.name)
        }

    private fun withPerformFunction(declaration: KoInterfaceDeclaration, commandName: String): Boolean =
        declaration.containsFunction {
            it.name == "perform" &&
                it.parameters.size == 1 &&
                it.parameters.all { param -> (param.type.name == commandName) }
        }

    private fun isNamedDispatcher(dispatcher: KoInterfaceDeclaration): Boolean = dispatcher.name == "Dispatcher"
}
