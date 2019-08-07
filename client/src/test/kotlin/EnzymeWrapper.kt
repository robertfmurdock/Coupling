import com.zegreatrob.coupling.client.external.react.ComponentBuilder
import com.zegreatrob.coupling.client.external.react.ComponentProvider
import com.zegreatrob.coupling.client.external.react.ReactFunctionComponent
import com.zegreatrob.coupling.client.external.react.component
import react.RBuilder
import react.RClass
import react.RProps
import react.buildElement

external interface Enzyme {
    fun shallow(element: dynamic): ShallowWrapper<dynamic>
}

external interface ShallowWrapper<T> {
    fun <T2 : RProps> find(target: RClass<T2>): ShallowWrapper<T2>
    fun <T2> find(target: dynamic): ShallowWrapper<T2>

    val length: Int

    fun props(): T

    fun update(): ShallowWrapper<T>

    fun debug(): String

    fun text(): String

    fun simulate(eventName: String)
    fun simulate(eventName: String, event: dynamic)

    fun <O> map(mapper: (ShallowWrapper<T>) -> O): Array<O>

    fun <T> find(mapper: (ShallowWrapper<T>) -> Boolean): ShallowWrapper<T>
    fun hasClass(className: String): Boolean
    fun prop(key: String): Any
    fun at(index: Int): ShallowWrapper<T>
}

@JsModule("enzyme")

external val enzyme: Enzyme

fun shallowRender(function: RBuilder.() -> Unit) = enzyme.shallow(buildElement(function))

fun <P : RProps> ComponentBuilder<P>.shallow(props: P) = shallowRender { component(build(), props) }

fun <P : RProps> ShallowWrapper<dynamic>.findComponent(
        reactFunctionComponent: ReactFunctionComponent<P>
): ShallowWrapper<P> = find(reactFunctionComponent.rFunction)

fun <P : RProps> ShallowWrapper<dynamic>.findComponent(
        componentProvider: ComponentProvider<P>
): ShallowWrapper<P> = find(componentProvider.component.rFunction)
