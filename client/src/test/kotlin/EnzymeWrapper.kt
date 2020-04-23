import com.zegreatrob.coupling.client.external.react.ComponentBuilder
import com.zegreatrob.coupling.client.external.react.RComponent
import com.zegreatrob.coupling.client.external.react.ReactFunctionComponent
import com.zegreatrob.coupling.client.external.react.child
import react.RBuilder
import react.RClass
import react.RProps
import react.buildElement
import kotlin.js.Json
import kotlin.js.json

external interface Enzyme {
    fun shallow(element: dynamic): ShallowWrapper<dynamic>

    fun configure(config: Json)
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
    fun shallow(): ShallowWrapper<T>
}

fun <T> ShallowWrapper<T>.simulateInputChange(fieldName: String, fieldValue: String) {
    return findInputByName(fieldName)
        .simulate(
            "change",
            json(
                "target" to json("name" to fieldName, "value" to fieldValue),
                "persist" to {}
            )
        )
}

fun <T> ShallowWrapper<T>.findByClass(className: String) = find<T>(".${className}")
fun <T> ShallowWrapper<T>.findInputByName(inputName: String) = find<T>("input[name='${inputName}']")

@JsModule("enzyme")
external val enzyme: Enzyme


@JsModule("enzyme-adapter-react-16")
external class Adapter

fun shallowRender(function: RBuilder.() -> Unit) = enzyme.shallow(buildElement(function))

fun <P : RProps> ComponentBuilder<P>.shallow(props: P) = shallowRender { child(build(), props) }

fun <P : RProps> shallow(component: RComponent<P>, props: P) = shallowRender { child(component, props) }

fun <P : RProps, T> ShallowWrapper<T>.findComponent(
    RComponent: RComponent<P>
): ShallowWrapper<P> = find(RComponent.component.rFunction)

val setup = object {
    init {
        enzyme.configure(json("adapter" to Adapter()))
    }
}
