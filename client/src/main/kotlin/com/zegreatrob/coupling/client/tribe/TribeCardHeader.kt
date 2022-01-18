package com.zegreatrob.coupling.client.tribe

import com.zegreatrob.coupling.client.CardHeader
import com.zegreatrob.coupling.client.Paths.tribeConfigPath
import com.zegreatrob.coupling.client.external.react.get
import com.zegreatrob.coupling.client.external.react.useStyles
import com.zegreatrob.coupling.client.fitty.fitty
import com.zegreatrob.coupling.model.tribe.Tribe
import com.zegreatrob.minreact.DataProps
import com.zegreatrob.minreact.TMFC
import com.zegreatrob.minreact.tmFC
import org.w3c.dom.Node
import react.ChildrenBuilder
import react.router.dom.Link

private val styles = useStyles("tribe/TribeCard")

data class TribeCardHeader(val tribe: Tribe, val size: Int) : DataProps<TribeCardHeader> {
    override val component: TMFC<TribeCardHeader> get() = tribeCardHeader
}

val tribeCardHeader = tmFC<TribeCardHeader> { (tribe, size) ->
    CardHeader {
        this.size = size
        this.className = styles["header"]
        this.linkUrl = tribe.tribeConfigPath()
        this.headerContent = tribe.name ?: ""
    }
}
