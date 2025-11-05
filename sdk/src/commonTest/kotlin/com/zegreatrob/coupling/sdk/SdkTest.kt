package com.zegreatrob.coupling.sdk

import com.apollographql.apollo.api.json.BufferedSinkJsonWriter
import com.apollographql.apollo.api.toJson
import com.zegreatrob.coupling.model.user.UserId
import com.zegreatrob.coupling.sdk.gql.GqlQuery
import com.zegreatrob.coupling.sdk.schema.UserDetailsQuery
import com.zegreatrob.coupling.sdk.schema.type.buildUser
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.action.DispatcherPipeCannon
import com.zegreatrob.testmints.async.asyncSetup
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import kotools.types.text.toNotBlankString
import okio.Buffer
import kotlin.test.Test
import kotlin.uuid.Uuid

class SdkTest {

    @Test
    fun sdkWillSendTraceAsHeader() = asyncSetup(object {
        var lastRequestId: String? = null
        val engine = MockEngine { request ->
            lastRequestId = request.headers["X-Request-Id"]
            val data = UserDetailsQuery.Data {
                user = buildUser {
                    id = UserId.new()
                    email = "hi".toNotBlankString().getOrThrow()
                    connectedEmails = emptyList()
                    authorizedPartyIds = emptyList()
                    players = emptyList()
                    connectSecretId = null
                }
            }
            val sink = Buffer()
            val jsonWriter = BufferedSinkJsonWriter(sink)
            data.toJson(jsonWriter)
            sink.close()
            respond("{\"data\":${sink.readUtf8()}}")
        }
        val traceId = Uuid.random()
        lateinit var sdk: DispatcherPipeCannon<CouplingSdkDispatcher>
    }) {
        sdk = sdk(PRIMARY_AUTHORIZED_USER_EMAIL, primaryTestPassword, engine, traceId)
    } exercise {
        sdk.fire(GqlQuery(UserDetailsQuery()))
    } verify {
        lastRequestId.assertIsEqualTo(traceId.toString())
    }
}
