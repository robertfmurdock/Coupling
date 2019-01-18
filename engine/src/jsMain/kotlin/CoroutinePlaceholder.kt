
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.asDeferred
import kotlin.js.Promise

class CoroutinePlaceholder {

    fun <T> doThing(promise: Promise<T>): Deferred<T> {
        return promise.asDeferred()
    }

}