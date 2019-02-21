import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.runBlocking

actual fun <T> testAsync(block: suspend CoroutineScope.() -> T) {
    runBlocking { block() }
}