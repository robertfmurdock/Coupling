import com.zegreatrob.coupling.sdk.Sdk
import com.zegreatrob.coupling.sdk.SdkProviderSyntax
import com.zegreatrob.testmints.async.asyncTestTemplate

interface SdkContext : SdkProviderSyntax {
    override val sdk: Sdk
    val username: String
}

val sdkSetup = asyncTestTemplate<SdkContext>(beforeAll = {
    val sdk = authorizedSdk()
    object : SdkContext {
        override val username = primaryAuthorizedUsername
        override val sdk = sdk
    }
})
