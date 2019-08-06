import com.zegreatrob.coupling.client.CouplingRouterProps
import com.zegreatrob.coupling.client.GoogleSignIn
import com.zegreatrob.coupling.client.couplingRouter
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.promise
import react.RBuilder
import react.ReactElement
import react.buildElements

@Suppress("unused")
@JsName("components")
object ReactComponents : GoogleSignIn {

    @Suppress("unused")
    @JsName("CouplingRouter")
    val couplingRouterJs = jsReactFunction { props: dynamic ->
        couplingRouter(CouplingRouterProps(
                props.isSignedIn.unsafeCast<Boolean>(),
                props.animationsDisabled.unsafeCast<Boolean>()
        ))
    }

    @Suppress("unused")
    @JsName("googleCheckForSignedIn")
    fun googleCheckForSignedIn(): dynamic = GlobalScope.promise { checkForSignedIn() }

    private fun jsReactFunction(handler: RBuilder.(dynamic) -> ReactElement) = { props: dynamic ->
        buildElements { handler(props) }
    }

}
