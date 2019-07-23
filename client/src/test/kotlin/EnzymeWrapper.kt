import com.zegreatrob.coupling.client.ComponentBuilder
import com.zegreatrob.coupling.client.ComponentProvider
import com.zegreatrob.coupling.client.ReactFunctionComponent
import com.zegreatrob.coupling.client.component
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

    val length: Int

    fun props(): T

    fun update()

    fun debug(): String

    fun text(): String

    fun simulate(eventName: String)
    fun simulate(eventName: String, event: dynamic)

    fun <O> map(mapper: (ShallowWrapper<T>) -> O): Array<O>
}

@JsModule("enzyme")
@JsNonModule
external val enzyme: Enzyme

fun shallowRender(function: RBuilder.() -> Unit) = enzyme.shallow(buildElement(function))

fun <P : RProps> ComponentBuilder<P>.shallow(props: P) = shallowRender { component(build(), props) }

fun <P : RProps> ShallowWrapper<dynamic>.findComponent(
        reactFunctionComponent: ReactFunctionComponent<P>
): ShallowWrapper<P> = find(reactFunctionComponent.rFunction)

fun <P : RProps> ShallowWrapper<dynamic>.findComponent(
        componentProvider: ComponentProvider<P>
): ShallowWrapper<P> = find(componentProvider.component.rFunction)
