package com.zegreatrob.coupling.json

import com.zegreatrob.coupling.model.Contributor

fun Contributor.toJson() = GqlContributor(
    email = email,
    details = details?.toSerializable(),
)

fun GqlContributor.toModel() = Contributor(
    email = email,
    details = details?.toModel(),
)
