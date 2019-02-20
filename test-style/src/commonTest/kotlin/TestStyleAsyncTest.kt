import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class TestStyleAsyncTest {

    @Test
    fun canFailAsync() {
        try {
            testAsync {
                val exercisedContext = setupAsync(object {
                }) exerciseAsync {
                }

                exercisedContext verifyAsync { fail("LOL") }
            }
        } catch (expectedFailure: AssertionError) {
            assertEquals("LOL", expectedFailure.message)
        }
    }

    @Test
    fun canFailAsyncWithCoroutine() {
        try {
            testAsync {
                val exercisedContext = setupAsync(object {
                }) exerciseAsync {
                }

                exercisedContext verifyAsync {
                    withContext(Dispatchers.Default) {
                        delay(3)
                        fail("LOL")
                    }
                }
            }
        } catch (expectedFailure: AssertionError) {
            assertEquals("LOL", expectedFailure.message)
        }
    }

}