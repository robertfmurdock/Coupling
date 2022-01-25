import com.zegreatrob.coupling.repository.validation.MagicClock
import com.zegreatrob.coupling.repository.validation.SharedContext
import com.zegreatrob.coupling.repository.validation.SharedContextData
import com.zegreatrob.coupling.sdk.Sdk
import com.zegreatrob.coupling.sdk.authorizedKtorSdk
import com.zegreatrob.coupling.sdk.primaryAuthorizedUsername
import com.zegreatrob.coupling.stubmodel.stubUser
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.minassert.assertIsNotEqualTo
import com.zegreatrob.testmints.async.asyncTestTemplate
import kotlin.test.Test

class SdkUserRepositoryTest {

    val repositorySetup = asyncTestTemplate<SharedContext<Sdk>>(sharedSetup = {
        val clock = MagicClock()
        val sdk = authorizedKtorSdk()
        SharedContextData(sdk, clock, stubUser().copy(email = primaryAuthorizedUsername))
    })

    @Test
    fun willReturnTheUser() = repositorySetup({ it }) exercise {
        repository.getUser()
    } verify { result ->
        result?.data.let {
            it?.email.assertIsEqualTo(primaryAuthorizedUsername)
            it?.id.assertIsNotEqualTo(null)
            it?.authorizedTribeIds.assertIsNotEqualTo(null)
        }
    }

}
