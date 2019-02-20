import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class TestStyleTest {

    @Test
    fun verifyShouldThrowErrorWhenFailureOccurs() {
        val exercisedContext = setup(object {
        }) exercise {
        }

        try {
            exercisedContext verify { fail("LOL") }
        } catch (expectedFailure: AssertionError) {
            assertEquals("LOL", expectedFailure.message)
        }
    }

}