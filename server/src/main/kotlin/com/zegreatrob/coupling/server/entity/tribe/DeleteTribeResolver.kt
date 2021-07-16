package com.zegreatrob.coupling.server.entity.tribe

import com.zegreatrob.coupling.json.TribeInput
import com.zegreatrob.coupling.server.action.connection.DeleteTribeCommand
import com.zegreatrob.coupling.server.external.graphql.Resolver
import com.zegreatrob.coupling.server.graphql.DispatcherProviders.tribeCommand
import com.zegreatrob.coupling.server.graphql.dispatch
import kotlinx.serialization.Serializable

val deleteTribeResolver: Resolver = dispatch(tribeCommand, { _, _: DeleteTribeInput -> DeleteTribeCommand }, { true })

@Serializable
data class DeleteTribeInput(override val tribeId: String): TribeInput
