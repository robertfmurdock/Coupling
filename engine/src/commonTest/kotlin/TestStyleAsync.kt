
import kotlinx.coroutines.CoroutineScope

expect fun <T> testAsync(block: suspend CoroutineScope.() -> T): Any

class SetupAsync<C>(val context: C)
class ExerciseAsync<C, R>(val context: C, val result: R)

suspend fun <C> setupAsync(context: C) = SetupAsync(context)

suspend infix fun <C, R> SetupAsync<C>.exerciseAsync(codeUnderTest: suspend C.() -> R) =
        context.codeUnderTest()
                .let { ExerciseAsync(context, it) }

suspend infix fun <T, R, R2> ExerciseAsync<T, R>.verifyAsync(assertionFunctions: suspend T.(R) -> R2) =
        context.assertionFunctions(result)
