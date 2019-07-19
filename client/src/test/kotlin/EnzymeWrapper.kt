
import com.zegreatrob.coupling.client.ReactFunctionComponent
import com.zegreatrob.coupling.client.component
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

    fun text(): String
}

@JsModule("enzyme")
@JsNonModule
external val enzyme: Enzyme

fun <P : RProps> ReactFunctionComponent<P>.shallowRender(props: P) =
        enzyme.shallow(buildElement { component(this@shallowRender, props) })
