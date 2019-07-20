import com.zegreatrob.coupling.client.component
import com.zegreatrob.coupling.client.pairassignments.ComponentBuilder
import react.RBuilder
import react.RClass
import react.RProps
import react.buildElement

external interface Enzyme {
    fun shallow(element: dynamic): ShallowWrapper<dynamic>
}

external interface ShallowWrapper<T> {
    fun <T2 : RProps> find(target: RClass<T2>): ShallowWrapper<T2>
    fun find(target: dynamic): ShallowWrapper<dynamic>

    fun props(): T

    fun update()

    fun debug(): String

    fun text(): String

    fun simulate(eventName: String)
}

@JsModule("enzyme")
@JsNonModule
external val enzyme: Enzyme

fun shallowRender(function: RBuilder.() -> Unit) = enzyme.shallow(buildElement(function))

fun <P : RProps> ComponentBuilder<P>.shallow(props: P) = shallowRender { component(build(), props) }

