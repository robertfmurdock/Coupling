// Vernacular based on http://xunitpatterns.com/Four%20Phase%20Test.html

class Exercise<C, R>(val context: C, val result: R)
class Setup<C>(val context: C)

fun <C> setup(context: C) = Setup(context)

infix fun <C, R> Setup<C>.exercise(codeUnderTest: C.() -> R) = context.codeUnderTest()
        .let { Exercise(context, it) }

infix fun <T, R, R2> Exercise<T, R>.verify(assertionFunctions: T.(R) -> R2) = context.assertionFunctions(result)