import com.zegreatrob.coupling.client.App
import com.zegreatrob.coupling.client.GoogleSignIn

@Suppress("unused")
@JsName("components")
object ReactComponents : GoogleSignIn {

    @Suppress("unused")
    @JsName("bootstrapApp")
    fun bootstrapApp() = App.bootstrapApp()

}
